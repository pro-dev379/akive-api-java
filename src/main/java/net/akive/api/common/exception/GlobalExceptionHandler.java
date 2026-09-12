package net.akive.api.common.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import net.akive.api.common.exception.business.BusinessException;
import net.akive.api.common.response.ApiErrorResponse;
import net.akive.api.common.response.FieldErrorDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.akive.api.common.exception.ApplicationErrorHttpStatusMapper.toHttpStatus;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_INPUT_VALUE;
import static net.akive.api.common.exception.business.ResourceNotFoundError.ENDPOINT_NOT_FOUND;
import static net.akive.api.common.exception.internal.UnexpectedServerError.INTERNAL_SERVER_ERROR;
import static net.akive.api.common.exception.web.HttpRequestError.METHOD_NOT_ALLOWED;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiErrorResponse> handleBusinessException(HttpServletRequest request, BusinessException e) {
        ApplicationError error = e.getError();
        logWarn(request, "[BusinessException]", e, "code=" + error.getCode() + ", detail=" + e.getDetail());

        return createErrorResponse(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidationException(
            HttpServletRequest request,
            MethodArgumentNotValidException e
    ) {
        List<FieldErrorDetail> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorDetail(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        logInfo(request, "[ValidationException]", "errors=" + fieldErrors);

        return createErrorResponse(INVALID_INPUT_VALUE, fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiErrorResponse> handleConstraintViolationException(
            HttpServletRequest request,
            ConstraintViolationException e
    ) {
        List<FieldErrorDetail> fieldErrors = e.getConstraintViolations()
                .stream()
                .map(violation -> new FieldErrorDetail(
                        resolveConstraintViolationFieldName(violation.getPropertyPath().toString()),
                        violation.getMessage()
                ))
                .toList();

        logInfo(request, "[ValidationException]", "errors=" + fieldErrors);

        return createErrorResponse(INVALID_INPUT_VALUE, fieldErrors);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestValueException.class
    })
    ResponseEntity<ApiErrorResponse> handleInvalidInputException(HttpServletRequest request, Exception e) {
        List<FieldErrorDetail> fieldErrors = resolveInvalidInputFieldErrors(e);
        logWarn(request, "[InvalidInputException]", e, "type=" + e.getClass().getSimpleName());

        if (fieldErrors.isEmpty()) {
            return createErrorResponse(INVALID_INPUT_VALUE);
        }

        return createErrorResponse(INVALID_INPUT_VALUE, fieldErrors);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNoResourceFoundException(HttpServletRequest request) {
        logWarn(request, "[NoResourceFoundException]", null, null);

        return createErrorResponse(ENDPOINT_NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiErrorResponse> handleMethodNotAllowedException(
            HttpServletRequest request,
            HttpRequestMethodNotSupportedException e
    ) {
        logWarn(request, "[InvalidInputException]", e, "supportedMethods=" + e.getSupportedHttpMethods());

        return createErrorResponse(METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleException(HttpServletRequest request, Exception e) {
        log.error("[UnexpectedException] uri={}, method={}", request.getRequestURI(), request.getMethod(), e);

        return createErrorResponse(INTERNAL_SERVER_ERROR);
    }

    private List<FieldErrorDetail> resolveInvalidInputFieldErrors(Exception e) {
        if (e instanceof IllegalArgumentException illegalArgumentException) {
            return resolveIllegalArgumentFieldErrors(illegalArgumentException);
        }

        if (e instanceof MethodArgumentTypeMismatchException typeMismatchException) {
            return resolveTypeMismatchFieldErrors(typeMismatchException);
        }

        if (e instanceof HttpMessageNotReadableException messageNotReadableException) {
            return resolveMessageNotReadableFieldErrors(messageNotReadableException);
        }

        if (e instanceof MissingRequestValueException missingRequestValueException) {
            return resolveMissingRequestValueFieldErrors(missingRequestValueException);
        }

        return List.of();
    }

    private List<FieldErrorDetail> resolveIllegalArgumentFieldErrors(IllegalArgumentException e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            message = "Invalid value.";
        }

        return List.of(new FieldErrorDetail("argument", message));
    }

    private List<FieldErrorDetail> resolveTypeMismatchFieldErrors(MethodArgumentTypeMismatchException e) {
        return List.of(new FieldErrorDetail(e.getName(), resolveInvalidFormatMessage(e.getRequiredType())));
    }

    private List<FieldErrorDetail> resolveMessageNotReadableFieldErrors(HttpMessageNotReadableException e) {
        if (!(e.getCause() instanceof InvalidFormatException invalidFormatException)) {
            return List.of();
        }

        return List.of(new FieldErrorDetail(
                        resolveFieldName(invalidFormatException),
                        resolveInvalidFormatMessage(invalidFormatException)
                ));
    }

    private List<FieldErrorDetail> resolveMissingRequestValueFieldErrors(MissingRequestValueException e) {
        return List.of(new FieldErrorDetail(resolveMissingRequestValueFieldName(e), "Required value is missing."));
    }

    private String resolveConstraintViolationFieldName(String propertyPath) {
        int lastDotIndex = propertyPath.lastIndexOf('.');

        if (lastDotIndex == -1) {
            return propertyPath;
        }

        return propertyPath.substring(lastDotIndex + 1);
    }

    private String resolveMissingRequestValueFieldName(MissingRequestValueException e) {
        if (e instanceof MissingServletRequestParameterException missingParameterException) {
            return missingParameterException.getParameterName();
        }

        if (e instanceof MissingPathVariableException missingPathVariableException) {
            return missingPathVariableException.getVariableName();
        }

        if (e instanceof MissingRequestHeaderException missingRequestHeaderException) {
            return missingRequestHeaderException.getHeaderName();
        }

        if (e instanceof MissingRequestCookieException missingRequestCookieException) {
            return missingRequestCookieException.getCookieName();
        }

        return "request";
    }

    private String resolveFieldName(JsonMappingException e) {
        return e.getPath()
                .stream()
                .map(JsonMappingException.Reference::getFieldName)
                .filter(fieldName -> fieldName != null && !fieldName.isBlank())
                .collect(Collectors.joining("."));
    }

    private String resolveInvalidFormatMessage(InvalidFormatException e) {
        return resolveInvalidFormatMessage(e.getTargetType());
    }

    private String resolveInvalidFormatMessage(Class<?> targetType) {
        if (targetType == null) {
            return "Invalid value.";
        }

        if (targetType.isEnum()) {
            return resolveEnumMismatchMessage(targetType);
        }

        if (LocalDate.class.equals(targetType)) {
            return "Invalid date format. Expected yyyy-MM-dd.";
        }

        return "Invalid value.";
    }

    private String resolveEnumMismatchMessage(Class<?> enumType) {
        String allowedValues = Arrays.stream(enumType.getEnumConstants())
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        return "Invalid value. Allowed values: " + allowedValues + ".";
    }

    private void logWarn(HttpServletRequest request, String tag, Exception e, String extraMessage) {
        String message = String.format("%s uri=%s, method=%s%s",
                tag,
                request.getRequestURI(),
                request.getMethod(),
                extraMessage != null ? ", " + extraMessage : ""
        );

        if (e != null) {
            log.warn(message, e);
        } else {
            log.warn(message);
        }
    }

    private void logInfo(HttpServletRequest request, String tag, String extraMessage) {
        log.info("{} uri={}, method={}, {}", tag, request.getRequestURI(), request.getMethod(), extraMessage);
    }

    private ResponseEntity<ApiErrorResponse> createErrorResponse(ApplicationError error) {
        return ResponseEntity
                .status(toHttpStatus(error))
                .body(ApiErrorResponse.of(error));
    }

    private ResponseEntity<ApiErrorResponse> createErrorResponse(
            ApplicationError error,
            List<FieldErrorDetail> fieldErrorDetails
    ) {
        return ResponseEntity
                .status(toHttpStatus(error))
                .body(ApiErrorResponse.of(error, fieldErrorDetails));
    }
}
