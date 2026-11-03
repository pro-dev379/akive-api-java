package net.akive.api.core.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.akive.api.common.exception.web.HttpRequestError;
import net.akive.api.common.response.ApiErrorResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.akive.api.common.exception.ApplicationErrorHttpStatusMapper.toHttpStatus;
import static net.akive.api.common.exception.web.HttpRequestError.INVALID_CSRF_PROTECTION_HEADER;
import static net.akive.api.common.logging.SecurityEventOutcome.DENIED;
import static net.akive.api.common.logging.SecurityEventType.CSRF_PROTECTION_REJECTED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@Slf4j(topic = "SECURITY_EVENT")
public class CookieRequestProtectionFilter extends OncePerRequestFilter {

    public static final String CSRF_PROTECTION_HEADER_NAME = "X-CSRF-Protection";
    public static final String CSRF_PROTECTION_HEADER_VALUE = "1";
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private final ObjectMapper objectMapper;
    private final RequestMatcher protectedEndpoints = new OrRequestMatcher(
            PathPatternRequestMatcher.withDefaults().matcher("/auth/login/**"),
            PathPatternRequestMatcher.withDefaults().matcher("/auth/tokens"),
            PathPatternRequestMatcher.withDefaults().matcher("/auth/tokens/**")
    );

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return SAFE_METHODS.contains(request.getMethod()) || !protectedEndpoints.matches(request);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (!CSRF_PROTECTION_HEADER_VALUE.equals(request.getHeader(CSRF_PROTECTION_HEADER_NAME))) {
            logCsrfProtectionFailure(request);
            writeErrorResponse(response, INVALID_CSRF_PROTECTION_HEADER);

            return;
        }

        filterChain.doFilter(request, response);
    }

    private void logCsrfProtectionFailure(HttpServletRequest request) {
        log.atWarn()
                .addKeyValue("eventType", CSRF_PROTECTION_REJECTED.name())
                .addKeyValue("outcome", DENIED.name())
                .addKeyValue("errorCode", INVALID_CSRF_PROTECTION_HEADER.getCode())
                .addKeyValue("httpMethod", request.getMethod())
                .addKeyValue("requestPath", request.getRequestURI())
                .log("Cookie-authenticated request rejected.");
    }

    private void writeErrorResponse(HttpServletResponse response, HttpRequestError error) throws IOException {
        response.setStatus(toHttpStatus(error).value());
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(UTF_8.name());

        objectMapper.writeValue(response.getWriter(), ApiErrorResponse.of(error));
    }
}
