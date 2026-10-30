package net.akive.api.core.config.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import net.akive.api.auth.adapter.login.webapi.LoginApi;
import net.akive.api.auth.adapter.login.webapi.dto.request.LoginByEmailRequest;
import net.akive.api.auth.adapter.token.cookie.RefreshTokenCookieManager;
import net.akive.api.auth.adapter.token.webapi.TokenIssueApi;
import net.akive.api.auth.adapter.token.webapi.TokenRevokeApi;
import net.akive.api.auth.application.login.provided.LoginProcessor;
import net.akive.api.auth.application.token.provided.RefreshTokenSessionManager;
import net.akive.api.auth.application.token.provided.TokenIssuer;
import net.akive.api.auth.application.token.provided.dto.TokenReissueCommand;
import net.akive.api.auth.domain.token.RefreshToken;
import net.akive.api.common.exception.ApplicationError;
import net.akive.api.legal.adapter.webapi.LegalDocumentActionApi;
import net.akive.api.legal.adapter.webapi.LegalDocumentApi;
import net.akive.api.legal.application.provided.LegalDocumentActionFinder;
import net.akive.api.legal.application.provided.LegalDocumentFinder;
import net.akive.api.legal.domain.LegalDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.List;
import java.util.UUID;

import static net.akive.api.auth.adapter.login.webapi.fixture.LoginByEmailRequestFixture.createLoginByEmailRequest;
import static net.akive.api.auth.adapter.token.cookie.RefreshTokenCookieManager.REFRESH_TOKEN_COOKIE_NAME;
import static net.akive.api.auth.domain.token.IssuedTokenFixture.createIssuedToken;
import static net.akive.api.auth.domain.token.RefreshTokenFixture.REFRESH_TOKEN_VALUE;
import static net.akive.api.auth.domain.token.RefreshTokenFixture.createRefreshToken;
import static net.akive.api.auth.domain.token.ReissuedTokenFixture.createReissuedTokenWithoutRotatedRefreshToken;
import static net.akive.api.common.exception.business.AuthenticationError.INVALID_ACCESS_TOKEN;
import static net.akive.api.common.exception.web.HttpRequestError.INVALID_CSRF_PROTECTION_HEADER;
import static net.akive.api.core.config.security.CookieRequestProtectionFilter.CSRF_PROTECTION_HEADER_NAME;
import static net.akive.api.core.config.security.CookieRequestProtectionFilter.CSRF_PROTECTION_HEADER_VALUE;
import static net.akive.api.legal.domain.LegalDocumentActionContext.SIGN_UP;
import static net.akive.api.legal.domain.LegalDocumentType.SERVICE_TERMS;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.LEGAL_DOCUMENT_ID;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.reconstructLegalDocument;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebMvcTest(
        controllers = {
                LoginApi.class,
                TokenIssueApi.class,
                TokenRevokeApi.class,
                LegalDocumentApi.class,
                LegalDocumentActionApi.class
        },
        properties = {
                "cors.allowed-origins[0]=http://localhost:3001",
                "cors.allowed-origins[1]=https://dev-www.akive.net",
                "cors.allowed-origins[2]=https://www.akive.net"
        }
)
@AutoConfigureMockMvc
@Import({ SecurityConfig.class, TokenAuthenticationEntryPoint.class })
class SecurityConfigTest {

    @Autowired
    MockMvcTester mvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    JwtDecoder accessTokenJwtDecoder;

    @MockitoBean
    LoginProcessor loginProcessor;

    @MockitoBean
    TokenIssuer tokenIssuer;

    @MockitoBean
    RefreshTokenSessionManager refreshTokenSessionManager;

    @MockitoBean
    RefreshTokenCookieManager refreshTokenCookieManager;

    @MockitoBean
    LegalDocumentActionFinder legalDocumentActionFinder;

    @MockitoBean
    LegalDocumentFinder legalDocumentFinder;

    @Test
    void 퍼블릭_엔드포인트는_Authorization_헤더가_있어도_액세스_토큰을_검증하지_않는다() throws JsonProcessingException {
        LoginByEmailRequest request = createLoginByEmailRequest();
        var issuedToken = createIssuedToken();

        given(loginProcessor.loginByEmail(any())).willReturn(issuedToken);
        given(refreshTokenCookieManager.create(issuedToken.refreshToken()))
                .willReturn(refreshTokenCookie(issuedToken.refreshToken().value()));

        mvcTester.post()
                .uri("/auth/login/email")
                .header(AUTHORIZATION, "Bearer invalid-access-token")
                .header("X-Client-Instance-Id", UUID.randomUUID().toString())
                .header(CSRF_PROTECTION_HEADER_NAME, CSRF_PROTECTION_HEADER_VALUE)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .assertThat()
                .hasStatus(OK);

        then(accessTokenJwtDecoder).shouldHaveNoInteractions();
    }

    @Test
    void 프라이빗_엔드포인트는_액세스_토큰을_검증한다() {
        given(accessTokenJwtDecoder.decode("invalid-access-token"))
                .willThrow(new BadJwtException("Invalid token"));

        MvcTestResultAssert result = mvcTester.get()
                .uri("/private")
                .header(AUTHORIZATION, "Bearer invalid-access-token")
                .assertThat()
                .hasStatus(UNAUTHORIZED);

        assertErrorResponse(result, INVALID_ACCESS_TOKEN);
    }

    @Test
    void 회원의_현재_약관_및_정책_문서_응답_조회_API는_액세스_토큰이_필요하다() {
        mvcTester.get()
                .uri("/legal-documents/1/legal-document-actions/current")
                .assertThat()
                .hasStatus(UNAUTHORIZED);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/legal-documents/1",
            "/legal-documents/types/SERVICE_TERMS/history/headers",
            "/legal-documents/types/SERVICE_TERMS/current/id",
            "/legal-documents/action-contexts/SIGN_UP/current/headers"
    })
    void 약관_및_정책_문서_조회_API는_액세스_토큰이_필요하지_않다(String url) {
        givenLegalDocumentFinder();

        mvcTester.get()
                .uri(url)
                .assertThat()
                .hasStatus(OK);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost:3001",
            "https://dev-www.akive.net",
            "https://www.akive.net"
    })
    void 허용된_출처의_Credential을_포함한_CORS_요청을_허용한다(String origin) {
        mvcTester.options()
                .uri("/private")
                .header(ORIGIN, origin)
                .header(ACCESS_CONTROL_REQUEST_METHOD, POST.name())
                .assertThat()
                .hasStatus(OK)
                .hasHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin)
                .hasHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
    }

    @Test
    void 허용되지_않은_출처의_CORS_사전_요청을_거부한다() {
        mvcTester.options()
                .uri("/private")
                .header(ORIGIN, "https://malicious.example")
                .header(ACCESS_CONTROL_REQUEST_METHOD, POST.name())
                .assertThat()
                .hasStatus(FORBIDDEN);
    }

    @Test
    void 리프레시_토큰_쿠키로_재발급할_때_액세스_토큰을_검증하지_않는다() {
        RefreshToken refreshToken = createRefreshToken();
        TokenReissueCommand command = new TokenReissueCommand(refreshToken);
        given(refreshTokenCookieManager.read(REFRESH_TOKEN_VALUE)).willReturn(refreshToken);
        given(tokenIssuer.reissue(command)).willReturn(createReissuedTokenWithoutRotatedRefreshToken());

        mvcTester.post()
                .uri("/auth/tokens")
                .header(AUTHORIZATION, "Bearer invalid-access-token")
                .header(CSRF_PROTECTION_HEADER_NAME, CSRF_PROTECTION_HEADER_VALUE)
                .cookie(new Cookie(REFRESH_TOKEN_COOKIE_NAME, REFRESH_TOKEN_VALUE))
                .assertThat()
                .hasStatus(OK);

        then(tokenIssuer).should().reissue(command);
        then(accessTokenJwtDecoder).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/auth/tokens/refresh", "/auth/tokens/refresh/current"})
    void 리프레시_토큰_쿠키로_리프레시_토큰_세션을_삭제할_때_액세스_토큰을_검증하지_않는다(String url) {
        RefreshToken refreshToken = createRefreshToken();
        given(refreshTokenCookieManager.read(REFRESH_TOKEN_VALUE)).willReturn(refreshToken);
        given(refreshTokenCookieManager.expire()).willReturn(refreshTokenCookie(""));

        mvcTester.delete()
                .uri(url)
                .header(AUTHORIZATION, "Bearer invalid-access-token")
                .header(CSRF_PROTECTION_HEADER_NAME, CSRF_PROTECTION_HEADER_VALUE)
                .cookie(new Cookie(REFRESH_TOKEN_COOKIE_NAME, REFRESH_TOKEN_VALUE))
                .assertThat()
                .hasStatus(NO_CONTENT);

        if (url.endsWith("/current")) {
            then(refreshTokenSessionManager).should().revokeCurrent(refreshToken);
        } else {
            then(refreshTokenSessionManager).should().revokeAll(refreshToken);
        }
        then(accessTokenJwtDecoder).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @CsvSource({
            "POST, /auth/login/email",
            "POST, /auth/tokens",
            "DELETE, /auth/tokens/refresh",
            "DELETE, /auth/tokens/refresh/current"
    })
    void CSRF_보호_대상_API는_CSRF_보호_헤더가_없으면_요청을_거부한다(String method, String url) {
        MvcTestResultAssert result = mvcTester.method(org.springframework.http.HttpMethod.valueOf(method))
                .uri(url)
                .assertThat()
                .hasStatus(FORBIDDEN);

        assertErrorResponse(result, INVALID_CSRF_PROTECTION_HEADER);
    }

    @ParameterizedTest
    @CsvSource({
            "POST, /auth/login/email",
            "POST, /auth/tokens",
            "DELETE, /auth/tokens/refresh",
            "DELETE, /auth/tokens/refresh/current"
    })
    void CSRF_보호_대상_API는_CSRF_보호_헤더_값이_유효하지_않으면_요청을_거부한다(String method, String url) {
        MvcTestResultAssert result = mvcTester.method(org.springframework.http.HttpMethod.valueOf(method))
                .uri(url)
                .header(CSRF_PROTECTION_HEADER_NAME, "Invalid")
                .assertThat()
                .hasStatus(FORBIDDEN);

        assertErrorResponse(result, INVALID_CSRF_PROTECTION_HEADER);
    }

    private void assertErrorResponse(MvcTestResultAssert result, ApplicationError error) {
        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo(error.getCode());
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo(error.getMessage());
    }

    private void givenLegalDocumentFinder() {
        LegalDocument legalDocument = reconstructLegalDocument();
        given(legalDocumentFinder.findOrThrow(LEGAL_DOCUMENT_ID)).willReturn(legalDocument);
        given(legalDocumentFinder.findAllHistoryByDocumentType(SERVICE_TERMS))
                .willReturn(List.of(legalDocument));
        given(legalDocumentFinder.findCurrentIdByDocumentType(SERVICE_TERMS))
                .willReturn(LEGAL_DOCUMENT_ID);
        given(legalDocumentFinder.findAllCurrentByContext(SIGN_UP))
                .willReturn(List.of(legalDocument));
    }

    private ResponseCookie refreshTokenCookie(String value) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, value).build();
    }
}
