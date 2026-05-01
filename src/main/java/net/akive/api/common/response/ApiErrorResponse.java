package net.akive.api.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.akive.api.common.exception.ErrorCode;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(String code, String message, List<FieldErrorDetail> errors) {

    public static ApiErrorResponse of(ErrorCode errorCode) {
        return new ApiErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                List.of()
        );
    }

    public static ApiErrorResponse of(ErrorCode errorCode, List<FieldErrorDetail> errors) {
        return new ApiErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                errors
        );
    }
}
