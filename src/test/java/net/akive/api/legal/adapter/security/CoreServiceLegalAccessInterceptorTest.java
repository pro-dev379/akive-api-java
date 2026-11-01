package net.akive.api.legal.adapter.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.akive.api.common.exception.business.AuthorizationException;
import net.akive.api.legal.application.provided.LegalRequirementChecker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Set;

import static net.akive.api.common.exception.business.AuthorizationError.CORE_SERVICE_LEGAL_REQUIREMENTS_NOT_SATISFIED;
import static net.akive.api.legal.application.provided.dto.LegalRequirementCheckResult.allSatisfied;
import static net.akive.api.legal.application.provided.dto.LegalRequirementCheckResult.withUnsatisfiedDocuments;
import static net.akive.api.legal.domain.LegalDocumentType.SERVICE_TERMS;
import static net.akive.api.legal.domain.LegalRequirementScope.CORE_SERVICE;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CoreServiceLegalAccessInterceptorTest {

    @Mock
    LegalRequirementChecker legalRequirementChecker;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    Object handler;

    @InjectMocks
    CoreServiceLegalAccessInterceptor coreServiceLegalAccessInterceptor;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 인증되지_않은_요청은_기본_서비스_이용_요건을_검사하지_않는다() {
        boolean allowed = coreServiceLegalAccessInterceptor.preHandle(request, response, handler);

        assertThat(allowed).isTrue();
        then(legalRequirementChecker).shouldHaveNoInteractions();
    }

    @Test
    void 기본_서비스_이용_요건을_충족한_회원의_요청을_허용한다() {
        authenticate(USER_ID);
        given(legalRequirementChecker.check(USER_ID, CORE_SERVICE)).willReturn(allSatisfied());

        boolean allowed = coreServiceLegalAccessInterceptor.preHandle(request, response, handler);

        assertThat(allowed).isTrue();
    }

    @Test
    void 기본_서비스_이용_요건을_충족하지_않은_회원의_요청을_거부한다() {
        authenticate(USER_ID);
        given(legalRequirementChecker.check(USER_ID, CORE_SERVICE))
                .willReturn(withUnsatisfiedDocuments(Set.of(SERVICE_TERMS)));

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> coreServiceLegalAccessInterceptor.preHandle(request, response, handler))
                .extracting(AuthorizationException::getError)
                .isEqualTo(CORE_SERVICE_LEGAL_REQUIREMENTS_NOT_SATISFIED);
    }

    private void authenticate(Long userId) {
        Jwt jwt = Jwt.withTokenValue("access-token")
                .header("alg", "none")
                .subject(String.valueOf(userId))
                .build();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }
}
