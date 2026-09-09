package net.akive.api.plan.application.service.totalquantity;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.totalquantity.dto.WeeklyPlanItemFindQuery;
import net.akive.api.plan.application.required.WeeklyQuantityRepository;
import net.akive.api.plan.application.required.data.WeeklyPlanData;
import net.akive.api.plan.application.result.WeeklyPlan;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.WEEKLY_PLAN_ITEM_NOT_FOUND;
import static net.akive.api.plan.application.fixture.PlanDataFixture.createWeeklyPlanData;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.SESSIONAL_PLAN_ID;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.createReconstructedSessionalPlan;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.FIRST_TOTAL_QUANTITY_ID;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.createReconstructedTotalQuantity;
import static net.akive.api.plan.domain.fixture.WeeklyQuantityFixture.FIRST_WEEKLY_QUANTITY_ID;
import static net.akive.api.plan.domain.fixture.WeeklyQuantityFixture.UNKNOWN_WEEKLY_QUANTITY_ID;
import static net.akive.api.user.domain.UserFixture.USER_ID;
import static net.akive.api.user.domain.UserFixture.createReconstructedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class WeeklyQuantityQueryServiceTest {

    @Mock
    WeeklyQuantityRepository weeklyQuantityRepository;

    @InjectMocks
    WeeklyQuantityQueryService weeklyQuantityQueryService;

    @Test
    void 주간_계획량을_조회한다() {
        User user = createReconstructedUser(USER_ID);
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID, user);
        TotalQuantity totalQuantity = createReconstructedTotalQuantity(FIRST_TOTAL_QUANTITY_ID, sessionalPlan);
        WeeklyPlanData weeklyPlanData = createWeeklyPlanData(SESSIONAL_PLAN_ID, totalQuantity);
        WeeklyPlanItemFindQuery query = new WeeklyPlanItemFindQuery(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID
        );

        given(weeklyQuantityRepository
                .findWeeklyPlanDataBySessionalPlanIdAndUserIdAndTotalQuantityIdAndWeeklyQuantityId(
                        SESSIONAL_PLAN_ID,
                        USER_ID,
                        FIRST_TOTAL_QUANTITY_ID,
                        FIRST_WEEKLY_QUANTITY_ID
                ))
                .willReturn(Optional.of(weeklyPlanData));

        WeeklyPlan.WeeklyPlanItem weeklyPlanItem = weeklyQuantityQueryService.findWeeklyPlanItem(query);

        assertThat(weeklyPlanItem.sessionalPlanId()).isEqualTo(weeklyPlanData.sessionalPlanId());
        assertThat(weeklyPlanItem.totalQuantityId()).isEqualTo(weeklyPlanData.totalQuantityId());
        assertThat(weeklyPlanItem.subject()).isEqualTo(weeklyPlanData.subject());
        assertThat(weeklyPlanItem.quantityUnit()).isEqualTo(weeklyPlanData.quantityUnit());
        assertThat(weeklyPlanItem.weeklyQuantity()).isEqualTo(weeklyPlanData.weeklyQuantity());

        then(weeklyQuantityRepository).should()
                .findWeeklyPlanDataBySessionalPlanIdAndUserIdAndTotalQuantityIdAndWeeklyQuantityId(
                        SESSIONAL_PLAN_ID,
                        USER_ID,
                        FIRST_TOTAL_QUANTITY_ID,
                        FIRST_WEEKLY_QUANTITY_ID
                );
    }

    @Test
    void 주간_계획량이_없으면_조회에_실패하고_예외를_던진다() {
        WeeklyPlanItemFindQuery query = new WeeklyPlanItemFindQuery(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID,
                UNKNOWN_WEEKLY_QUANTITY_ID
        );

        given(weeklyQuantityRepository
                .findWeeklyPlanDataBySessionalPlanIdAndUserIdAndTotalQuantityIdAndWeeklyQuantityId(
                        SESSIONAL_PLAN_ID,
                        USER_ID,
                        FIRST_TOTAL_QUANTITY_ID,
                        UNKNOWN_WEEKLY_QUANTITY_ID
                ))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> weeklyQuantityQueryService.findWeeklyPlanItem(query))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(WEEKLY_PLAN_ITEM_NOT_FOUND);

        then(weeklyQuantityRepository).should()
                .findWeeklyPlanDataBySessionalPlanIdAndUserIdAndTotalQuantityIdAndWeeklyQuantityId(
                        SESSIONAL_PLAN_ID,
                        USER_ID,
                        FIRST_TOTAL_QUANTITY_ID,
                        UNKNOWN_WEEKLY_QUANTITY_ID
                );
    }
}
