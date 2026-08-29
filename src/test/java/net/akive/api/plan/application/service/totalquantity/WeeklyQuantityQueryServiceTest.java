package net.akive.api.plan.application.service.totalquantity;

import net.akive.api.common.exception.business.NotFoundException;
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

import static net.akive.api.common.exception.business.NotFoundError.WEEKLY_PLAN_ITEM_NOT_FOUND;
import static net.akive.api.plan.domain.PlanFixture.createPersistedSessionalPlan;
import static net.akive.api.plan.domain.PlanFixture.createPersistedTotalQuantity;
import static net.akive.api.user.domain.UserFixture.createPersistedUser;
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
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 1L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createPersistedTotalQuantity(totalQuantityId, sessionalPlan);
        WeeklyPlanData weeklyPlanData = createWeeklyPlanData(sessionalPlanId, totalQuantity);
        WeeklyPlanItemFindQuery query = new WeeklyPlanItemFindQuery(
                sessionalPlanId,
                userId,
                totalQuantityId,
                weeklyQuantityId
        );

        given(weeklyQuantityRepository
                .findWeeklyPlanDataBySessionalPlanIdAndUserIdAndTotalQuantityIdAndWeeklyQuantityId(
                        sessionalPlanId,
                        userId,
                        totalQuantityId,
                        weeklyQuantityId
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
                        sessionalPlanId,
                        userId,
                        totalQuantityId,
                        weeklyQuantityId
                );
    }

    @Test
    void 주간_계획량이_없으면_조회에_실패하고_예외를_던진다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 999L;
        WeeklyPlanItemFindQuery query = new WeeklyPlanItemFindQuery(
                sessionalPlanId,
                userId,
                totalQuantityId,
                weeklyQuantityId
        );

        given(weeklyQuantityRepository
                .findWeeklyPlanDataBySessionalPlanIdAndUserIdAndTotalQuantityIdAndWeeklyQuantityId(
                        sessionalPlanId,
                        userId,
                        totalQuantityId,
                        weeklyQuantityId
                ))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> weeklyQuantityQueryService.findWeeklyPlanItem(query))
                .extracting(NotFoundException::getError)
                .isEqualTo(WEEKLY_PLAN_ITEM_NOT_FOUND);

        then(weeklyQuantityRepository).should()
                .findWeeklyPlanDataBySessionalPlanIdAndUserIdAndTotalQuantityIdAndWeeklyQuantityId(
                        sessionalPlanId,
                        userId,
                        totalQuantityId,
                        weeklyQuantityId
                );
    }

    private WeeklyPlanData createWeeklyPlanData(Long sessionalPlanId, TotalQuantity totalQuantity) {
        return new WeeklyPlanData(
                sessionalPlanId,
                totalQuantity.getTotalQuantityId(),
                totalQuantity.getSubject(),
                totalQuantity.getTotalQuantityAmount().quantityUnit(),
                totalQuantity.getWeeklyQuantities().getFirst()
        );
    }
}
