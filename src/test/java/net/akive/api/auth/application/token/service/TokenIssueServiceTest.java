package net.akive.api.auth.application.token.service;

import net.akive.api.auth.application.token.provided.RefreshTokenSessionManager;
import net.akive.api.auth.application.token.provided.dto.TokenIssueCommand;
import net.akive.api.auth.application.token.provided.dto.TokenReissueCommand;
import net.akive.api.auth.application.token.required.AccessTokenCreator;
import net.akive.api.auth.application.token.required.RefreshTokenParser;
import net.akive.api.auth.domain.token.*;
import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.user.domain.RoleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.auth.application.token.provided.dto.TokenIssueCommandFixture.createTokenIssueCommand;
import static net.akive.api.auth.application.token.provided.dto.TokenReissueCommandFixture.createTokenReissueCommand;
import static net.akive.api.auth.domain.token.AccessTokenFixture.createAccessToken;
import static net.akive.api.auth.domain.token.RefreshTokenFixture.createRefreshToken;
import static net.akive.api.auth.domain.token.RefreshTokenPayloadFixture.createRefreshTokenPayload;
import static net.akive.api.common.domain.ClientInstanceIdFixture.createClientInstanceId;
import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TokenIssueServiceTest {

    @Mock
    RefreshTokenSessionManager refreshTokenSessionManager;

    @Mock
    AccessTokenCreator accessTokenCreator;

    @Mock
    RefreshTokenParser refreshTokenParser;

    @InjectMocks
    TokenIssueService tokenIssueService;

    @Test
    void 액세스_토큰과_리프레시_토큰을_발급한다() {
        Long userId = 1L;
        ClientInstanceId clientInstanceId = createClientInstanceId();
        RoleType roleType = USER;
        TokenIssueCommand command = createTokenIssueCommand(userId, clientInstanceId, roleType);

        AccessToken accessToken = createAccessToken();
        RefreshToken refreshToken = createRefreshToken();

        given(accessTokenCreator.create(userId, roleType)).willReturn(accessToken);
        given(refreshTokenSessionManager.start(userId, clientInstanceId, roleType)).willReturn(refreshToken);

        IssuedToken result = tokenIssueService.issue(command);

        assertThat(result.accessToken()).isEqualTo(accessToken);
        assertThat(result.refreshToken()).isEqualTo(refreshToken);

        then(accessTokenCreator).should().create(userId, roleType);
        then(refreshTokenSessionManager).should().start(userId, clientInstanceId, roleType);
    }

    @Test
    void 토큰_재발급_시_액세스_토큰과_교체된_리프레시_토큰을_반환한다() {
        Long userId = 1L;
        ClientInstanceId clientInstanceId = createClientInstanceId();
        RoleType roleType = USER;
        RefreshToken currentRefreshToken = createRefreshToken("current-refresh-token");
        TokenReissueCommand command = createTokenReissueCommand(currentRefreshToken);

        RefreshTokenPayload payload = createRefreshTokenPayload(userId, clientInstanceId, roleType);
        AccessToken newAccessToken = createAccessToken("new-access-token");
        RefreshToken rotatedRefreshToken = createRefreshToken("rotated-refresh-token");

        given(refreshTokenParser.parse(currentRefreshToken)).willReturn(payload);
        given(accessTokenCreator.create(userId, roleType)).willReturn(newAccessToken);
        given(refreshTokenSessionManager.rotateIfRequired(payload, currentRefreshToken))
                .willReturn(Optional.of(rotatedRefreshToken));

        ReissuedToken result = tokenIssueService.reissue(command);

        assertThat(result.accessToken()).isEqualTo(newAccessToken);
        assertThat(result.rotatedRefreshToken()).isEqualTo(rotatedRefreshToken);

        then(refreshTokenParser).should().parse(currentRefreshToken);
        then(accessTokenCreator).should().create(userId, roleType);
        then(refreshTokenSessionManager).should().rotateIfRequired(payload, currentRefreshToken);
    }

    @Test
    void 토큰_재발급_시_리프레시_토큰_교체가_필요하지_않으면_액세스_토큰만_반환한다() {
        Long userId = 1L;
        ClientInstanceId clientInstanceId = createClientInstanceId();
        RoleType roleType = USER;
        RefreshToken currentRefreshToken = createRefreshToken("current-refresh-token");
        TokenReissueCommand command = createTokenReissueCommand(currentRefreshToken);

        RefreshTokenPayload payload = createRefreshTokenPayload(userId, clientInstanceId, roleType);
        AccessToken newAccessToken = createAccessToken("new-access-token");

        given(refreshTokenParser.parse(currentRefreshToken)).willReturn(payload);
        given(accessTokenCreator.create(userId, roleType)).willReturn(newAccessToken);
        given(refreshTokenSessionManager.rotateIfRequired(payload, currentRefreshToken))
                .willReturn(Optional.empty());

        ReissuedToken result = tokenIssueService.reissue(command);

        assertThat(result.accessToken()).isEqualTo(newAccessToken);
        assertThat(result.rotatedRefreshToken()).isNull();

        then(refreshTokenParser).should().parse(currentRefreshToken);
        then(accessTokenCreator).should().create(userId, roleType);
        then(refreshTokenSessionManager).should().rotateIfRequired(payload, currentRefreshToken);
    }
}
