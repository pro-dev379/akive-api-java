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
    TOTAL_QUANTITY_LESS_THAN_COMPLETED_QUANTITY(
            PLAN,
            "Total quantity cannot be less than completed quantity."
    ),
    WEEKLY_QUANTITY_LESS_THAN_COMPLETED_QUANTITY(
            PLAN,
            "Weekly quantity cannot be less than completed quantity."
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
