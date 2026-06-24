package net.akive.api.auth.application.port.out.token;

import net.akive.api.auth.domain.token.TokenPayload;

public interface ValidateTokenPort {

    TokenPayload validateRefreshToken(String refreshToken);
}