package net.akive.api.common.exception.business;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ApplicationError;
import net.akive.api.common.exception.ApplicationErrorType;

import static net.akive.api.common.exception.ApplicationErrorType.BUSINESS_RULE_VIOLATION;

@Getter
@RequiredArgsConstructor
public enum BusinessRuleViolationError implements ApplicationError {

    INVALID_INPUT_VALUE("Invalid input value."),

    REQUIRED_VALUE_MISSING("Required value is missing."),
    BLANK_VALUE("Value must not be blank."),
    EMPTY_COLLECTION("Collection must not be empty."),
    MAX_LENGTH_EXCEEDED("Value is too long."),
    MIN_LENGTH_NOT_MET("Value is too short."),
    VALUE_NOT_POSITIVE("Value must be positive."),
    VALUE_NEGATIVE("Value must not be negative."),
    INVALID_DATE_RANGE("Invalid date range."),
    INVALID_FORMAT("Invalid format."),

    EMAIL_VERIFICATION_CODE_MISMATCH("Email verification code mismatch."),
    EMAIL_NOT_VERIFIED("Email is not verified."),
    EMAIL_VERIFICATION_INVALID_CODE_FORMAT("Invalid email verification code format."),

    INVALID_SESSION_PERIOD("Invalid session period."),
    SESSION_PERIOD_CHANGE_NOT_ALLOWED("Session period cannot be changed."),
    SESSION_PERIOD_MAX_MONTHS_EXCEEDED("Session period is too long."),
    IN_PROGRESS_SESSIONAL_PLAN_LIMIT_EXCEEDED("Too many in-progress sessional plans."),
    TOTAL_QUANTITY_LIMIT_EXCEEDED("Total quantity limit exceeded."),
    TOTAL_AMOUNT_MISMATCH("Total amount mismatch."),
    TOTAL_AMOUNT_LESS_THAN_COMPLETED_AMOUNT("Total amount cannot be less than completed amount."),
    WEEKLY_QUANTITY_PERIOD_OUT_OF_SESSION("Weekly quantity period is outside session period."),
    WEEKLY_AMOUNT_EXCEEDS_TOTAL_AMOUNT("Weekly amount exceeds total amount."),
    WEEKLY_AMOUNT_MISMATCH("Weekly amount mismatch."),
    WEEKLY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT("Weekly amount cannot be less than completed amount."),
    LAST_WEEKLY_QUANTITY_REDUCTION_NOT_ALLOWED("Last weekly quantity cannot be reduced."),
    WEEKLY_AMOUNT_INCREASE_NOT_ALLOWED("Weekly amount cannot be increased here."),
    WEEKLY_DELAY_RECEIVER_SAME_AS_SOURCE(
            "Weekly quantity receiving the delay must be different from source weekly quantity."
    ),
    WEEKLY_DELAY_AMOUNT_EXCEEDS_CURRENT_WEEKLY_DELAYED_AMOUNT(
            "Weekly delay amount exceeds current weekly delayed amount."
    ),
    INVALID_WEEK_PERIOD("Invalid week period."),
    LAST_DAILY_AMOUNT_REDUCTION_NOT_ALLOWED("Last daily amount cannot be reduced."),
    DAILY_AMOUNT_INCREASE_NOT_ALLOWED("Daily amount cannot be increased here."),
    DAILY_QUANTITY_DATE_OUT_OF_WEEK("Daily quantity date is outside week period."),
    DAILY_AMOUNT_EXCEEDS_WEEKLY_AMOUNT("Daily amount exceeds weekly amount."),
    DAILY_AMOUNT_MISMATCH("Daily amount mismatch."),
    DAILY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT("Daily amount cannot be less than completed amount."),
    DAILY_AMOUNT_TO_COMPLETE_EXCEEDS_DELAYED_AMOUNT("Daily amount to complete cannot exceed delayed amount."),
    DAILY_AMOUNT_TO_CANCEL_COMPLETION_EXCEEDS_COMPLETED_AMOUNT(
            "Daily amount to cancel completion cannot exceed completed amount."
    ),
    DAILY_DELAY_RECEIVER_SAME_AS_SOURCE(
            "Daily quantity receiving the delay must be different from source daily quantity."
    ),
    DAILY_DELAY_AMOUNT_EXCEEDS_CURRENT_DAILY_DELAYED_AMOUNT("Daily delay amount exceeds current daily delayed amount."),

    DAILY_LOG_FOCUS_TIME_EXCEEDS_DURATION("Daily log focus time exceeds duration."),
    DAILY_LOG_TIME_RANGE_TOO_LONG("Daily log time range is too long."),
    DAILY_LOG_DATE_IN_FUTURE("Daily log date must not be in the future."),
    DAILY_LOG_TIME_RANGE_IN_FUTURE("Daily log time range must not be in the future.")
    ;

    private final String message;

    @Override
    public ApplicationErrorType getType() {
        return BUSINESS_RULE_VIOLATION;
    }
}
