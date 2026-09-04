package net.akive.api.plan.application.service.totalquantity;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.totalquantity.dto.SessionalPlanTotalQuantityFindQuery;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import net.akive.api.plan.application.result.TotalQuantityProgress;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.TOTAL_QUANTITY_NOT_FOUND;
import static net.akive.api.plan.domain.PlanFixture.createPersistedSessionalPlan;
import static net.akive.api.plan.domain.PlanFixture.createPersistedTotalQuantity;
import static net.akive.api.user.domain.UserFixture.createPersistedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TotalQuantityQueryServiceTest {

    @Mock
    TotalQuantityRepository totalQuantityRepository;

    @InjectMocks
    TotalQuantityQueryService totalQuantityQueryService;

    @Test
    void 세션_총량을_조회한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createPersistedTotalQuantity(totalQuantityId, sessionalPlan);
        SessionalPlanTotalQuantityFindQuery query = new SessionalPlanTotalQuantityFindQuery(
                sessionalPlanId,
                userId,
                totalQuantityId
        );

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                sessionalPlanId,
                userId,
                totalQuantityId
        )).willReturn(Optional.of(totalQuantity));

        TotalQuantityProgress totalQuantityProgress = totalQuantityQueryService.find(query);

        assertThat(totalQuantityProgress.totalQuantityId()).isEqualTo(totalQuantityId);
        assertThat(totalQuantityProgress.subject()).isEqualTo(totalQuantity.getSubject());
        assertThat(totalQuantityProgress.totalQuantityAmount()).isEqualTo(totalQuantity.getTotalQuantityAmount());
        assertThat(totalQuantityProgress.weeklyQuantities())
                .extracting(weeklyQuantity -> weeklyQuantity.sessionWeek().weekNum())
                .containsExactly(1, 2, 3);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(sessionalPlanId, userId, totalQuantityId);
    }

    @Test
    void 세셔널_플랜이_없으면_세션_총량_조회에_실패하고_예외를_던진다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        SessionalPlanTotalQuantityFindQuery query = new SessionalPlanTotalQuantityFindQuery(
                sessionalPlanId,
                userId,
                totalQuantityId
        );

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantityQueryService.find(query))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(TOTAL_QUANTITY_NOT_FOUND);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(sessionalPlanId, userId, totalQuantityId);
    }

    @Test
    void 세션_총량이_없으면_조회에_실패하고_예외를_던진다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        SessionalPlanTotalQuantityFindQuery query = new SessionalPlanTotalQuantityFindQuery(
                sessionalPlanId,
                userId,
                totalQuantityId
        );

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                sessionalPlanId,
                userId,
                totalQuantityId
        )).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantityQueryService.find(query))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(TOTAL_QUANTITY_NOT_FOUND);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(sessionalPlanId, userId, totalQuantityId);
    }
}
