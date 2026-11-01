package net.akive.api.legal.adapter.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.AuthorizationException;
import net.akive.api.legal.application.provided.LegalRequirementChecker;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.HandlerInterceptor;

import static net.akive.api.common.exception.business.AuthorizationError.CORE_SERVICE_LEGAL_REQUIREMENTS_NOT_SATISFIED;
import static net.akive.api.legal.domain.LegalRequirementScope.CORE_SERVICE;

@RequiredArgsConstructor
class CoreServiceLegalAccessInterceptor implements HandlerInterceptor {

    private final LegalRequirementChecker legalRequirementChecker;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            return true;
        }

        Long userId = Long.valueOf(jwtAuthenticationToken.getToken().getSubject());
        boolean satisfied = legalRequirementChecker.check(userId, CORE_SERVICE).satisfied();

        if (!satisfied) {
            throw new AuthorizationException(CORE_SERVICE_LEGAL_REQUIREMENTS_NOT_SATISFIED);
        }

        return true;
    }
}
