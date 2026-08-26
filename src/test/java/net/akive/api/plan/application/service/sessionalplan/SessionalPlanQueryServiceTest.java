package net.akive.api.plan.application.service.sessionalplan;

import net.akive.api.common.exception.business.NotFoundException;
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

import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.plan.domain.PlanFixture.createPersistedSessionalPlan;
import static net.akive.api.plan.domain.PlanFixture.createPersistedTotalQuantity;
import static net.akive.api.user.domain.UserFixture.createPersistedUser;
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
        Long userId = 1L;
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(10L, createPersistedUser(userId));
        given(sessionalPlanRepository.findAllByUserIdOrderBySessionalPlanIdDesc(userId))
                .willReturn(List.of(sessionalPlan));

        List<SessionalPlan> sessionalPlans = sessionalPlanQueryService.findAll(userId);

        assertThat(sessionalPlans).containsExactly(sessionalPlan);

        then(sessionalPlanRepository).should().findAllByUserIdOrderBySessionalPlanIdDesc(userId);
    }

    @Test
    void 세셔널_플랜_상세를_조회한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createPersistedTotalQuantity(20L, sessionalPlan);
        SessionalPlanFindQuery query = new SessionalPlanFindQuery(userId, sessionalPlanId);

        given(sessionalPlanRepository.findBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.of(sessionalPlan));
        given(totalQuantityRepository.findAllBySessionalPlanIdOrderByTotalQuantityIdAsc(sessionalPlanId))
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

        then(sessionalPlanRepository).should().findBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should().findAllBySessionalPlanIdOrderByTotalQuantityIdAsc(sessionalPlanId);
    }

    @Test
    void 세셔널_플랜이_없으면_상세_조회에_실패하고_예외를_던진다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        SessionalPlanFindQuery query = new SessionalPlanFindQuery(userId, sessionalPlanId);
        given(sessionalPlanRepository.findBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> sessionalPlanQueryService.find(query))
                .extracting(NotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanRepository).should().findBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should(never())
                .findAllBySessionalPlanIdOrderByTotalQuantityIdAsc(sessionalPlanId);
    }
}
