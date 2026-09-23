package net.akive.api.plan.application.service.sessionalplan;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.sessionalplan.dto.CompletionRateFindQuery;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanDetailFindQuery;
import net.akive.api.plan.application.required.DailyQuantityRepository;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import net.akive.api.plan.application.result.SessionalPlanDetail;
import net.akive.api.plan.application.result.TotalQuantityProgress;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.value.CompletionRate;
import net.akive.api.plan.domain.value.QuantityCompletionAmount;
import net.akive.api.plan.domain.value.SessionPeriod;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.*;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.FIRST_TOTAL_QUANTITY_ID;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.createReconstructedTotalQuantity;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SessionalPlanQueryServiceTest {

    @Mock
    SessionalPlanRepository sessionalPlanRepository;

    @Mock
    TotalQuantityRepository totalQuantityRepository;

    @Mock
    DailyQuantityRepository dailyQuantityRepository;

    @InjectMocks
    SessionalPlanQueryService sessionalPlanQueryService;

    @Test
    void 회원_식별자로_세셔널_플랜_목록을_조회한다() {
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(
                SESSIONAL_PLAN_ID,
                createReconstructedUser(USER_ID)
        );
        given(sessionalPlanRepository.findAllByUserIdOrderBySessionalPlanIdDesc(USER_ID))
                .willReturn(List.of(sessionalPlan));

        List<SessionalPlan> sessionalPlans = sessionalPlanQueryService.findAll(USER_ID);

        assertThat(sessionalPlans).containsExactly(sessionalPlan);

        then(sessionalPlanRepository).should().findAllByUserIdOrderBySessionalPlanIdDesc(USER_ID);
    }

    @Test
    void 세셔널_플랜_상세를_조회한다() {
        User user = createReconstructedUser(USER_ID);
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID, user);
        TotalQuantity totalQuantity = createReconstructedTotalQuantity(FIRST_TOTAL_QUANTITY_ID, sessionalPlan);
        SessionalPlanDetailFindQuery query = new SessionalPlanDetailFindQuery(USER_ID, SESSIONAL_PLAN_ID);

        given(sessionalPlanRepository.findBySessionalPlanIdAndUserId(SESSIONAL_PLAN_ID, USER_ID))
                .willReturn(Optional.of(sessionalPlan));
        given(totalQuantityRepository.findAllBySessionalPlanIdOrderByTotalQuantityIdAsc(SESSIONAL_PLAN_ID))
                .willReturn(List.of(totalQuantity));

        SessionalPlanDetail sessionalPlanDetail = sessionalPlanQueryService.findDetail(query);

        assertThat(sessionalPlanDetail.sessionalPlan()).isEqualTo(sessionalPlan);
        assertThat(sessionalPlanDetail.totalQuantities()).hasSize(1);
        TotalQuantityProgress totalQuantityProgress = sessionalPlanDetail.totalQuantities().getFirst();
        assertThat(totalQuantityProgress.totalQuantityId()).isEqualTo(totalQuantity.getTotalQuantityId());
        assertThat(totalQuantityProgress.subject()).isEqualTo(totalQuantity.getSubject());
        assertThat(totalQuantityProgress.totalQuantityAmount()).isEqualTo(totalQuantity.getTotalQuantityAmount());
        assertThat(totalQuantityProgress.weeklyQuantities())
                .extracting(weeklyQuantity -> weeklyQuantity.sessionWeek().weekNum())
                .containsExactly(1, 2, 3);

        then(sessionalPlanRepository).should().findBySessionalPlanIdAndUserId(SESSIONAL_PLAN_ID, USER_ID);
        then(totalQuantityRepository).should().findAllBySessionalPlanIdOrderByTotalQuantityIdAsc(SESSIONAL_PLAN_ID);
    }

    @Test
    void 세셔널_플랜이_없으면_상세_조회에_실패하고_예외를_던진다() {
        SessionalPlanDetailFindQuery query = new SessionalPlanDetailFindQuery(USER_ID, UNKNOWN_SESSIONAL_PLAN_ID);
        given(sessionalPlanRepository.findBySessionalPlanIdAndUserId(UNKNOWN_SESSIONAL_PLAN_ID, USER_ID))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> sessionalPlanQueryService.findDetail(query))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanRepository).should().findBySessionalPlanIdAndUserId(UNKNOWN_SESSIONAL_PLAN_ID, USER_ID);
        then(totalQuantityRepository).should(never())
                .findAllBySessionalPlanIdOrderByTotalQuantityIdAsc(UNKNOWN_SESSIONAL_PLAN_ID);
    }

    @Test
    void 회원_식별자로_세션_기간_목록을_조회한다() {
        given(sessionalPlanRepository.findSessionPeriodsByUserId(USER_ID))
                .willReturn(List.of(SESSION_PERIOD));

        List<SessionPeriod> sessionPeriods = sessionalPlanQueryService.findSessionPeriodsByUserId(USER_ID);

        assertThat(sessionPeriods).containsExactly(SESSION_PERIOD);

        then(sessionalPlanRepository).should().findSessionPeriodsByUserId(USER_ID);
    }

    @Test
    void 조회_기간의_세션_총량별_계획량과_완료량으로_달성률을_조회한다() {
        LocalDate startOn = LocalDate.of(2026, 9, 14);
        LocalDate endOn = LocalDate.of(2026, 9, 20);
        CompletionRateFindQuery query = new CompletionRateFindQuery(USER_ID, startOn, endOn);
        List<QuantityCompletionAmount> completionAmounts =
                List.of(new QuantityCompletionAmount(100, 75));
        given(dailyQuantityRepository.findTotalQuantityCompletionAmountsByUserIdAndDateBetween(
                USER_ID,
                startOn,
                endOn
        )).willReturn(completionAmounts);

        CompletionRate completionRate = sessionalPlanQueryService.findCompletionRate(query);

        assertThat(completionRate.value()).isEqualByComparingTo("75.0");
        then(dailyQuantityRepository).should()
                .findTotalQuantityCompletionAmountsByUserIdAndDateBetween(USER_ID, startOn, endOn);
    }
}
