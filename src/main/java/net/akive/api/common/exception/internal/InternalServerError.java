package net.akive.api.common.exception.internal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.Error;
import net.akive.api.common.exception.ErrorDomain;
import org.springframework.http.HttpStatus;

import static net.akive.api.common.exception.ErrorDomain.COMMON;

@Getter
@RequiredArgsConstructor
public enum InternalServerError implements Error {

    INTERNAL_SERVER_ERROR(COMMON, "Unexpected server error.");

    private final ErrorDomain domain;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
