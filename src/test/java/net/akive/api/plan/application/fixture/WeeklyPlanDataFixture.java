package net.akive.api.plan.application.fixture;

import net.akive.api.plan.application.required.data.WeeklyPlanData;
import net.akive.api.plan.domain.model.TotalQuantity;

import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.createReconstructedSessionalPlan;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.FIRST_TOTAL_QUANTITY_ID;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.createReconstructedTotalQuantity;

public class WeeklyPlanDataFixture {

    private WeeklyPlanDataFixture() {
    }

    public static WeeklyPlanData createWeeklyPlanData(Long sessionalPlanId, TotalQuantity totalQuantity) {
        return new WeeklyPlanData(
                sessionalPlanId,
                totalQuantity.getTotalQuantityId(),
                totalQuantity.getSubject(),
                totalQuantity.getTotalQuantityAmount().quantityUnit(),
                totalQuantity.getWeeklyQuantities().getFirst()
        );
    }

    public static WeeklyPlanData createWeeklyPlanData(Long sessionalPlanId) {
        TotalQuantity totalQuantity = createReconstructedTotalQuantity(
                FIRST_TOTAL_QUANTITY_ID,
                createReconstructedSessionalPlan(sessionalPlanId)
        );

        return createWeeklyPlanData(sessionalPlanId, totalQuantity);
    }
}
