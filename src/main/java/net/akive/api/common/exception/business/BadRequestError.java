package net.akive.api.common.exception.business;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.Error;
import net.akive.api.common.exception.ErrorDomain;
import org.springframework.http.HttpStatus;

import static net.akive.api.common.exception.ErrorDomain.*;

@Getter
@RequiredArgsConstructor
public enum BadRequestError implements Error {

    INVALID_INPUT_VALUE(COMMON, "Invalid input value."),

    REQUIRED_VALUE_MISSING(COMMON_DOMAIN, "Required value is missing."),
    BLANK_VALUE(COMMON_DOMAIN, "Value must not be blank."),
    EMPTY_COLLECTION(COMMON_DOMAIN, "Collection must not be empty."),
    MAX_LENGTH_EXCEEDED(COMMON_DOMAIN, "Value is too long."),
    MIN_LENGTH_NOT_MET(COMMON_DOMAIN, "Value is too short."),
    VALUE_NOT_POSITIVE(COMMON_DOMAIN, "Value must be positive."),
    VALUE_NEGATIVE(COMMON_DOMAIN, "Value must not be negative."),
    INVALID_DATE_RANGE(COMMON_DOMAIN, "Invalid date range."),
    INVALID_FORMAT(COMMON_DOMAIN, "Invalid format."),

    EMAIL_VERIFICATION_CODE_MISMATCH(EMAIL_VERIFICATION, "Email verification code mismatch."),
    EMAIL_NOT_VERIFIED(EMAIL_VERIFICATION, "Email is not verified."),
    EMAIL_VERIFICATION_INVALID_CODE_FORMAT(EMAIL_VERIFICATION, "Invalid email verification code format."),

    INVALID_SESSION_PERIOD(PLAN, "Invalid session period."),
    SESSION_PERIOD_CHANGE_NOT_ALLOWED(PLAN, "Session period cannot be changed."),
    SESSION_PERIOD_MAX_MONTHS_EXCEEDED(PLAN, "Session period is too long."),
    IN_PROGRESS_SESSIONAL_PLAN_LIMIT_EXCEEDED(PLAN, "Too many in-progress sessional plans."),
    TOTAL_QUANTITY_LIMIT_EXCEEDED(PLAN, "Total quantity limit exceeded."),
    TOTAL_AMOUNT_MISMATCH(PLAN, "Total amount mismatch."),
    TOTAL_AMOUNT_LESS_THAN_COMPLETED_AMOUNT(
            PLAN,
            "Total amount cannot be less than completed amount."
    ),
    WEEKLY_QUANTITY_PERIOD_OUT_OF_SESSION(PLAN, "Weekly quantity period is outside session period."),
    WEEKLY_AMOUNT_EXCEEDS_TOTAL_AMOUNT(PLAN, "Weekly amount exceeds total amount."),
    WEEKLY_AMOUNT_MISMATCH(PLAN, "Weekly amount mismatch."),
    WEEKLY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT(
            PLAN,
            "Weekly amount cannot be less than completed amount."
    ),
    LAST_WEEKLY_QUANTITY_REDUCTION_NOT_ALLOWED(PLAN, "Last weekly quantity cannot be reduced."),
    WEEKLY_AMOUNT_INCREASE_NOT_ALLOWED(PLAN, "Weekly amount cannot be increased here."),
    WEEKLY_DELAY_RECEIVER_SAME_AS_SOURCE(
            PLAN,
            "Weekly quantity receiving the delay must be different from source weekly quantity."
    ),
    WEEKLY_DELAY_AMOUNT_EXCEEDS_CURRENT_WEEKLY_DELAYED_AMOUNT(
            PLAN,
            "Weekly delay amount exceeds current weekly delayed amount."
    ),
    INVALID_WEEK_PERIOD(PLAN, "Invalid week period."),
    LAST_DAILY_AMOUNT_REDUCTION_NOT_ALLOWED(PLAN, "Last daily amount cannot be reduced."),
    DAILY_AMOUNT_INCREASE_NOT_ALLOWED(PLAN, "Daily amount cannot be increased here."),
    DAILY_QUANTITY_DATE_OUT_OF_WEEK(PLAN, "Daily quantity date is outside week period."),
    DAILY_AMOUNT_EXCEEDS_WEEKLY_AMOUNT(PLAN, "Daily amount exceeds weekly amount."),
    DAILY_AMOUNT_MISMATCH(PLAN, "Daily amount mismatch."),
    DAILY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT(
            PLAN,
            "Daily amount cannot be less than completed amount."
    ),
    DAILY_AMOUNT_TO_COMPLETE_EXCEEDS_DELAYED_AMOUNT(
            PLAN,
            "Daily amount to complete cannot exceed delayed amount."
    ),
    DAILY_AMOUNT_TO_CANCEL_COMPLETION_EXCEEDS_COMPLETED_AMOUNT(
            PLAN,
            "Daily amount to cancel completion cannot exceed completed amount."
    ),
    DAILY_DELAY_RECEIVER_SAME_AS_SOURCE(
            PLAN,
            "Daily quantity receiving the delay must be different from source daily quantity."
    ),
    DAILY_DELAY_AMOUNT_EXCEEDS_CURRENT_DAILY_DELAYED_AMOUNT(
            PLAN,
            "Daily delay amount exceeds current daily delayed amount."
    )
    ;

    private final ErrorDomain domain;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
