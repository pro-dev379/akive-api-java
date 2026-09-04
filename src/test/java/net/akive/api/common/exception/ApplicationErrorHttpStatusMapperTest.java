package net.akive.api.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static net.akive.api.common.exception.ApplicationErrorHttpStatusMapper.toHttpStatus;
import static net.akive.api.common.exception.ApplicationErrorType.*;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicationErrorHttpStatusMapperTest {

    @Test
    void 애플리케이션_에러_타입을_HTTP_상태로_매핑한다() {
        assertThat(toHttpStatus(error(BUSINESS_RULE_VIOLATION))).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(toHttpStatus(error(RESOURCE_NOT_FOUND))).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(toHttpStatus(error(RESOURCE_CONFLICT))).isEqualTo(HttpStatus.CONFLICT);
        assertThat(toHttpStatus(error(AUTHENTICATION_ERROR))).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(toHttpStatus(error(RATE_LIMIT_EXCEEDED))).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(toHttpStatus(error(UNEXPECTED_SERVER_ERROR))).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(toHttpStatus(error(METHOD_NOT_ALLOWED))).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(toHttpStatus(error(UNSUPPORTED_MEDIA_TYPE))).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    private ApplicationError error(ApplicationErrorType type) {
        return new ApplicationError() {
            @Override
            public ApplicationErrorType getType() {
                return type;
            }

            @Override
            public String getMessage() {
                return "message";
            }
        };
    }
}
