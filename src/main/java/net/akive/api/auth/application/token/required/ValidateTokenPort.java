package net.akive.api.auth.application.token.required;

import net.akive.api.auth.domain.token.TokenPayload;

public interface ValidateTokenPort {

    TokenPayload validateRefreshToken(String refreshToken);
}
