package net.akive.api.plan.application.service.sessionalplan;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanFindQuery;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import net.akive.api.plan.application.result.SessionalPlanDetail;
import net.akive.api.plan.application.result.TotalQuantityProgress;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        SessionalPlanFindQuery query = new SessionalPlanFindQuery(USER_ID, SESSIONAL_PLAN_ID);

        given(sessionalPlanRepository.findBySessionalPlanIdAndUserId(SESSIONAL_PLAN_ID, USER_ID))
                .willReturn(Optional.of(sessionalPlan));
        given(totalQuantityRepository.findAllBySessionalPlanIdOrderByTotalQuantityIdAsc(SESSIONAL_PLAN_ID))
                .willReturn(List.of(totalQuantity));

        SessionalPlanDetail sessionalPlanDetail = sessionalPlanQueryService.find(query);

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
        SessionalPlanFindQuery query = new SessionalPlanFindQuery(USER_ID, UNKNOWN_SESSIONAL_PLAN_ID);
        given(sessionalPlanRepository.findBySessionalPlanIdAndUserId(UNKNOWN_SESSIONAL_PLAN_ID, USER_ID))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> sessionalPlanQueryService.find(query))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanRepository).should().findBySessionalPlanIdAndUserId(UNKNOWN_SESSIONAL_PLAN_ID, USER_ID);
        then(totalQuantityRepository).should(never())
                .findAllBySessionalPlanIdOrderByTotalQuantityIdAsc(UNKNOWN_SESSIONAL_PLAN_ID);
    }
}
