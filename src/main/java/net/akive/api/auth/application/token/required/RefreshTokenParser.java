package net.akive.api.auth.application.token.required;

import net.akive.api.auth.domain.token.RefreshToken;
import net.akive.api.auth.domain.token.RefreshTokenPayload;

public interface RefreshTokenParser {

    RefreshTokenPayload parse(RefreshToken refreshToken);
}
