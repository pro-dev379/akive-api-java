package net.akive.api.common.exception.business;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.Error;
import net.akive.api.common.exception.ErrorDomain;
import org.springframework.http.HttpStatus;

import static net.akive.api.common.exception.ErrorDomain.*;

@Getter
@RequiredArgsConstructor
public enum ConflictError implements Error {

    EMAIL_ALREADY_EXISTS(USER, "Email already exists."),
    NICKNAME_ALREADY_EXISTS(USER, "Nickname already exists"),

    SESSION_NAME_ALREADY_EXISTS(PLAN, "Session name already exists."),
    SUBJECT_NAME_ALREADY_EXISTS(PLAN, "Subject name already exists."),
    TOTAL_AMOUNT_LESS_THAN_COMPLETED_AMOUNT(
            PLAN,
            "Total amount cannot be less than completed amount."
    ),
    WEEKLY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT(
            PLAN,
            "Weekly amount cannot be less than completed amount."
    ),
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

    EMAIL_ALREADY_VERIFIED(EMAIL_VERIFICATION, "Email is already verified."),
    ;

    private final ErrorDomain domain;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}
