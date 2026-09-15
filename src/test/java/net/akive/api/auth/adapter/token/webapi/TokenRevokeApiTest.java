package net.akive.api.auth.adapter.token.webapi;

import net.akive.api.auth.application.token.provided.RefreshTokenSessionManager;
import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.support.PrivateApiTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;
import java.util.stream.Stream;

import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@WebMvcTest(TokenRevokeApi.class)
class TokenRevokeApiTest extends PrivateApiTestSupport {

    private static final String REFRESH_TOKEN_REVOKE_URL = "/auth/tokens/refresh";
    private static final String CURRENT_REFRESH_TOKEN_REVOKE_URL = "/auth/tokens/refresh/current";
    private static final String CLIENT_INSTANCE_ID_HEADER = "X-Client-Instance-Id";

    @MockitoBean
    RefreshTokenSessionManager refreshTokenSessionManager;

    @Test
    void 회원의_모든_리프레시_토큰을_무효화한다() {
        delete(REFRESH_TOKEN_REVOKE_URL, USER_ID).hasStatus(NO_CONTENT);

        then(refreshTokenSessionManager).should().revokeByUserId(USER_ID);
    }

    @Test
    void 회원의_리프레시_토큰_중_현재_클라이언트의_토큰만_무효화한다() {
        UUID clientInstanceId = UUID.randomUUID();
        ClientInstanceId commandClientInstanceId = new ClientInstanceId(clientInstanceId);

        delete(
                CURRENT_REFRESH_TOKEN_REVOKE_URL,
                USER_ID,
                CLIENT_INSTANCE_ID_HEADER,
                clientInstanceId.toString()
        ).hasStatus(NO_CONTENT);

        then(refreshTokenSessionManager)
                .should()
                .revokeByUserIdAndClientInstanceId(USER_ID, commandClientInstanceId);
    }

    @ParameterizedTest
    @MethodSource("invalidClientInstanceIdHeaders")
    void 현재_클라이언트의_리프레시_토큰만_무효화_시_클라이언트_식별자가_올바르지_않으면_400을_응답한다(
            String clientInstanceIdHeader
    ) {
        delete(
                CURRENT_REFRESH_TOKEN_REVOKE_URL,
                USER_ID,
                CLIENT_INSTANCE_ID_HEADER,
                clientInstanceIdHeader
        ).hasStatus(BAD_REQUEST);

        then(refreshTokenSessionManager).should(never()).revokeByUserIdAndClientInstanceId(any(), any());
    }

    private static Stream<String> invalidClientInstanceIdHeaders() {
        return Stream.of(
                null,
                "",
                "invalid-uuid"
        );
    }
}
