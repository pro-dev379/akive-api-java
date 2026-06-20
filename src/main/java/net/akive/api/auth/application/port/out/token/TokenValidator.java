package net.akive.api.auth.application.port.out.token;

import net.akive.api.auth.domain.token.TokenPayload;

public interface TokenValidator {

    TokenPayload validateRefreshToken(String refreshToken);
}