package net.akive.api.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.akive.api.common.response.ApiErrorResponse;
import net.akive.api.common.response.FieldErrorDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiErrorResponse> handleBusinessException(HttpServletRequest request, BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();

        log.warn(
                "[BusinessException] uri={}, method={}, code={}, message={}",
                request.getRequestURI(),
                request.getMethod(),
                errorCode.getCode(),
                e.getMessage(),
                e
        );

        return createErrorResponse(errorCode);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidationException(HttpServletRequest request, MethodArgumentNotValidException e) {
        List<FieldErrorDetail> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorDetail(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        log.info(
                "[ValidationException] uri={}, method={}, errors={}",
                request.getRequestURI(),
                request.getMethod(),
                fieldErrors,
                e
        );

        return createErrorResponse(CommonErrorCode.INVALID_INPUT_VALUE, fieldErrors);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestValueException.class
    })
    ResponseEntity<ApiErrorResponse> handleInvalidInputException(
            HttpServletRequest request,
            Exception e
    ) {
        log.warn(
                "[InvalidInputException] uri={}, method={}, exception={}, message={}",
                request.getRequestURI(),
                request.getMethod(),
                e.getClass().getSimpleName(),
                e.getMessage(),
                e
        );

        return createErrorResponse(CommonErrorCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNoResourceFoundException(HttpServletRequest request) {
        log.warn("[NoResourceFoundException] uri={}, method={}", request.getRequestURI(), request.getMethod());

        return createErrorResponse(CommonErrorCode.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleException(HttpServletRequest request, Exception e) {
        log.error("[UnexpectedException] uri={}, method={}", request.getRequestURI(), request.getMethod(), e);

        return createErrorResponse(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiErrorResponse> createErrorResponse(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiErrorResponse.of(errorCode));
    }

    private ResponseEntity<ApiErrorResponse> createErrorResponse(ErrorCode errorCode, List<FieldErrorDetail> fieldErrorDetails) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiErrorResponse.of(errorCode, fieldErrorDetails));
    }
}
