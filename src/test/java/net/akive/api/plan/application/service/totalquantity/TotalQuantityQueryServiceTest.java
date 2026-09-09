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
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.SESSIONAL_PLAN_ID;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.createReconstructedSessionalPlan;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.FIRST_TOTAL_QUANTITY_ID;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.createReconstructedTotalQuantity;
import static net.akive.api.user.domain.UserFixture.USER_ID;
import static net.akive.api.user.domain.UserFixture.createReconstructedUser;
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
        User user = createReconstructedUser(USER_ID);
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID, user);
        TotalQuantity totalQuantity = createReconstructedTotalQuantity(FIRST_TOTAL_QUANTITY_ID, sessionalPlan);
        SessionalPlanTotalQuantityFindQuery query = new SessionalPlanTotalQuantityFindQuery(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        );

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        )).willReturn(Optional.of(totalQuantity));

        TotalQuantityProgress totalQuantityProgress = totalQuantityQueryService.find(query);

        assertThat(totalQuantityProgress.totalQuantityId()).isEqualTo(FIRST_TOTAL_QUANTITY_ID);
        assertThat(totalQuantityProgress.subject()).isEqualTo(totalQuantity.getSubject());
        assertThat(totalQuantityProgress.totalQuantityAmount()).isEqualTo(totalQuantity.getTotalQuantityAmount());
        assertThat(totalQuantityProgress.weeklyQuantities())
                .extracting(weeklyQuantity -> weeklyQuantity.sessionWeek().weekNum())
                .containsExactly(1, 2, 3);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(SESSIONAL_PLAN_ID, USER_ID, FIRST_TOTAL_QUANTITY_ID);
    }

    @Test
    void 세션_총량이_없으면_조회에_실패하고_예외를_던진다() {
        SessionalPlanTotalQuantityFindQuery query = new SessionalPlanTotalQuantityFindQuery(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        );

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        )).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantityQueryService.find(query))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(TOTAL_QUANTITY_NOT_FOUND);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(SESSIONAL_PLAN_ID, USER_ID, FIRST_TOTAL_QUANTITY_ID);
    }
}
