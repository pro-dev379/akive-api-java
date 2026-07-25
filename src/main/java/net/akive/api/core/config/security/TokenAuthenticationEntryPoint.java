package net.akive.api.core.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.UnauthorizedError;
import net.akive.api.common.response.ApiErrorResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static net.akive.api.common.exception.business.UnauthorizedError.ACCESS_TOKEN_EXPIRED;
import static net.akive.api.common.exception.business.UnauthorizedError.INVALID_ACCESS_TOKEN;

@Component
@RequiredArgsConstructor
public class TokenAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        UnauthorizedError errorCode = resolveUnauthorizedErrorCode(authException);

        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getWriter(), ApiErrorResponse.of(errorCode));
    }

    private UnauthorizedError resolveUnauthorizedErrorCode(AuthenticationException authException) {
        if (containsExpiredMessage(authException)) {
            return ACCESS_TOKEN_EXPIRED;
        }

        return INVALID_ACCESS_TOKEN;
    }

    private boolean containsExpiredMessage(Throwable throwable) {
        while (throwable != null) {
            String message = throwable.getMessage();

            if (message != null && message.toLowerCase(Locale.ROOT).contains("expired")) {
                return true;
            }

            throwable = throwable.getCause();
        }

        return false;
    }
}
