package net.akive.api.common.exception.business;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.Error;
import net.akive.api.common.exception.ErrorDomain;
import org.springframework.http.HttpStatus;

import static net.akive.api.common.exception.ErrorDomain.EMAIL_VERIFICATION;

@Getter
@RequiredArgsConstructor
public enum TooManyRequestsError implements Error {

    EMAIL_VERIFICATION_RESEND_TOO_FREQUENT(EMAIL_VERIFICATION, "Email verification resend is too frequent."),
    EMAIL_VERIFICATION_RESEND_LIMIT_EXCEEDED(EMAIL_VERIFICATION, "Email verification resend limit exceeded."),
    ;

    private final ErrorDomain domain;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.TOO_MANY_REQUESTS;
    }
}