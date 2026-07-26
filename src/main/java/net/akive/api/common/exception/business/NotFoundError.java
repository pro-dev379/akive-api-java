package net.akive.api.common.exception.business;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.Error;
import net.akive.api.common.exception.ErrorDomain;
import org.springframework.http.HttpStatus;

import static net.akive.api.common.exception.ErrorDomain.*;

@Getter
@RequiredArgsConstructor
public enum NotFoundError implements Error {

    RESOURCE_NOT_FOUND(COMMON, "Resource not found."),
    USER_NOT_FOUND(USER, "User not found."),
    SESSIONAL_PLAN_NOT_FOUND(PLAN, "Sessional plan not found."),
    SESSIONAL_PLAN_TOTAL_QUANTITY_NOT_FOUND(PLAN, "Total quantity not found."),
    WEEKLY_PLAN_NOT_FOUND(PLAN, "Weekly plan not found."),
    EMAIL_VERIFICATION_NOT_FOUND(EMAIL_VERIFICATION, "Email verification not found."),
    ;

    private final ErrorDomain domain;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
