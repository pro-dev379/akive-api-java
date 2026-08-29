package net.akive.api.plan.adapter.webapi.common;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class PlanApiPath {

    public static final String SESSIONAL_PLANS = "/sessional-plans";
    public static final String SESSIONAL_PLAN = SESSIONAL_PLANS + "/{sessionalPlanId}";
    public static final String DAILY_PLANS = SESSIONAL_PLANS + "/daily-plans";
    public static final String DAILY_PLAN = DAILY_PLANS + "/{date}";
    public static final String WEEKLY_PLANS = SESSIONAL_PLAN + "/weekly-plans";
    public static final String WEEKLY_PLAN = WEEKLY_PLANS + "/{weekNum}";
    public static final String TOTAL_QUANTITIES = SESSIONAL_PLAN + "/total-quantities";
    public static final String TOTAL_QUANTITY = TOTAL_QUANTITIES + "/{totalQuantityId}";
    public static final String WEEKLY_QUANTITIES = TOTAL_QUANTITY + "/weekly-quantities";
    public static final String WEEKLY_QUANTITY = WEEKLY_QUANTITIES + "/{weeklyQuantityId}";
    public static final String WEEKLY_QUANTITY_DELAYS = WEEKLY_QUANTITY + "/delays";
}
