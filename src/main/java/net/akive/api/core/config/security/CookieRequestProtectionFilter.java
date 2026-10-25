package net.akive.api.core.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

public class CookieRequestProtectionFilter extends OncePerRequestFilter {

    public static final String CSRF_PROTECTION_HEADER_NAME = "X-CSRF-Protection";
    public static final String CSRF_PROTECTION_HEADER_VALUE = "1";

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
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
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
