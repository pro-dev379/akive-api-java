package net.akive.api.plan.domain.policy;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class PlanPolicy {

    public static final int SESSION_NAME_MAX_LENGTH = 50;
    public static final int SUBJECT_NAME_MAX_LENGTH = 50;
    public static final int SUBJECT_CATEGORY_MAX_LENGTH = 20;
    public static final int QUANTITY_UNIT_MAX_LENGTH = 10;
    public static final int QUANTITY_MEMO_MAX_LENGTH = 100;
    public static final long IN_PROGRESS_SESSIONAL_PLAN_MAX_COUNT = 5L;
    public static final long TOTAL_QUANTITY_MAX_COUNT = 50L;
    public static final int SESSION_PERIOD_MAX_MONTHS = 12;
    public static final long DAYS_PER_WEEK = 7L;
}
