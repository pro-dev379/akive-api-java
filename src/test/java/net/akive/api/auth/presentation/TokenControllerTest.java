package net.akive.api.auth.presentation;

import net.akive.api.auth.application.dto.result.token.ReissueTokenResult;
import net.akive.api.common.exception.business.UnauthorizedException;
import net.akive.api.support.PublicControllerTestSupport;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.business.UnauthorizedError.EXPIRED_REFRESH_TOKEN;
import static net.akive.api.common.exception.business.UnauthorizedError.INVALID_REFRESH_TOKEN;
import static net.akive.api.common.exception.business.BadRequestError.INVALID_INPUT_VALUE;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TokenControllerTest extends PublicControllerTestSupport {

    @Test
    void 리프레시_토큰으로_토큰을_재발행한다() throws Exception {
        // given
        String refreshToken = "refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        given(issueTokenUseCase.reissue(refreshToken))
                .willReturn(new ReissueTokenResult(newAccessToken, newRefreshToken));

        // when & then
        mockMvc.perform(post("/tokens")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.refreshToken").value(newRefreshToken));

        then(issueTokenUseCase).should().reissue(refreshToken);
    }

    @Test
    void 리프레시_토큰_재발행_시_새_리프레시_토큰이_없으면_응답에서_제외한다() throws Exception {
        // given
        String refreshToken = "refresh-token";
        String newAccessToken = "new-access-token";

        given(issueTokenUseCase.reissue(refreshToken))
                .willReturn(new ReissueTokenResult(newAccessToken, null));

        // when & then
        mockMvc.perform(post("/tokens")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    void 토큰_재발행_요청_시_Bearer_토큰의_앞뒤_공백을_제거한다() throws Exception {
        // given
        String refreshToken = "refresh-token";
        String newAccessToken = "new-access-token";

        given(issueTokenUseCase.reissue(refreshToken))
                .willReturn(new ReissueTokenResult(newAccessToken, null));

        // when & then
        mockMvc.perform(post("/tokens")
                        .header("Authorization", "Bearer  " + refreshToken + " "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(newAccessToken));

        then(issueTokenUseCase).should().reissue(refreshToken);
    }

    @Test
    void 토큰_재발행_요청_시_Authorization_헤더가_없으면_400을_응답한다() throws Exception {
        // when & then
        mockMvc.perform(post("/tokens"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("$.message").value(INVALID_INPUT_VALUE.getMessage()));
    }

    @Test
    void 토큰_재발행_요청_시_Authorization_헤더가_Bearer_형식이_아니면_401을_응답한다() throws Exception {
        // when & then
        mockMvc.perform(post("/tokens")
                        .header("Authorization", "refresh-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_REFRESH_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(INVALID_REFRESH_TOKEN.getMessage()));
    }

    @Test
    void 토큰_재발행_요청_시_Bearer_토큰이_비어있으면_401을_응답한다() throws Exception {
        // when & then
        mockMvc.perform(post("/tokens")
                        .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_REFRESH_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(INVALID_REFRESH_TOKEN.getMessage()));
    }

    @Test
    void 토큰_재발행_요청_시_유효하지_않은_리프레시_토큰이면_401을_응답한다() throws Exception {
        // given
        String refreshToken = "invalid-refresh-token";

        willThrow(new UnauthorizedException(INVALID_REFRESH_TOKEN))
                .given(issueTokenUseCase)
                .reissue(refreshToken);

        // when & then
        mockMvc.perform(post("/tokens")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_REFRESH_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(INVALID_REFRESH_TOKEN.getMessage()));
    }

    @Test
    void 토큰_재발행_요청_시_만료된_리프레시_토큰이면_401을_응답한다() throws Exception {
        // given
        String refreshToken = "expired-refresh-token";

        willThrow(new UnauthorizedException(EXPIRED_REFRESH_TOKEN))
                .given(issueTokenUseCase)
                .reissue(refreshToken);

        // when & then
        mockMvc.perform(post("/tokens")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(EXPIRED_REFRESH_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(EXPIRED_REFRESH_TOKEN.getMessage()));
    }
}
