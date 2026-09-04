package net.akive.api.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.akive.api.common.exception.ApplicationError;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@JsonInclude(NON_EMPTY)
public record ApiErrorResponse(String code, String message, List<FieldErrorDetail> errors) {

    public static ApiErrorResponse of(ApplicationError error) {
        return new ApiErrorResponse(
                error.getCode(),
                error.getMessage(),
                List.of()
        );
    }

    public static ApiErrorResponse of(ApplicationError error, List<FieldErrorDetail> errors) {
        return new ApiErrorResponse(
                error.getCode(),
                error.getMessage(),
                errors
        );
    }
}
