package net.akive.api.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.akive.api.common.exception.Error;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@JsonInclude(NON_EMPTY)
public record ApiErrorResponse(String code, String message, List<FieldErrorDetail> errors) {

    public static ApiErrorResponse of(Error error) {
        return new ApiErrorResponse(
                error.getCode(),
                error.getMessage(),
                List.of()
        );
    }

    public static ApiErrorResponse of(Error error, List<FieldErrorDetail> errors) {
        return new ApiErrorResponse(
                error.getCode(),
                error.getMessage(),
                errors
        );
    }
}
