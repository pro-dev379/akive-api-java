package net.akive.api.core.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.common.response.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static net.akive.api.auth.exception.TokenErrorCode.EXPIRED_ACCESS_TOKEN;
import static net.akive.api.auth.exception.TokenErrorCode.INVALID_ACCESS_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;

class TokenAuthenticationEntryPointTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TokenAuthenticationEntryPoint tokenAuthenticationEntryPoint = new TokenAuthenticationEntryPoint(objectMapper);

    @Test
    void 액세스_토큰이_만료되면_만료된_액세스_토큰_예외를_던진다() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        tokenAuthenticationEntryPoint.commence(
                new MockHttpServletRequest(),
                response,
                new BadCredentialsException("Jwt expired at 2026-07-03T00:00:00Z")
        );

        ApiErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ApiErrorResponse.class);

        assertThat(response.getStatus()).isEqualTo(EXPIRED_ACCESS_TOKEN.getStatus().value());
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(errorResponse.code()).isEqualTo(EXPIRED_ACCESS_TOKEN.getCode());
        assertThat(errorResponse.message()).isEqualTo(EXPIRED_ACCESS_TOKEN.getMessage());
    }

    @Test
    void 액세스_토큰이_유효하지_않으면_유효하지_않은_액세스_토큰_예외를_던진다() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        tokenAuthenticationEntryPoint.commence(
                new MockHttpServletRequest(),
                response,
                new BadCredentialsException("Invalid bearer token")
        );

        ApiErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ApiErrorResponse.class);

        assertThat(response.getStatus()).isEqualTo(INVALID_ACCESS_TOKEN.getStatus().value());
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(errorResponse.code()).isEqualTo(INVALID_ACCESS_TOKEN.getCode());
        assertThat(errorResponse.message()).isEqualTo(INVALID_ACCESS_TOKEN.getMessage());
    }
}
