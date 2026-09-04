package net.akive.api.auth.application.token.required;

import net.akive.api.auth.domain.token.RefreshToken;
import net.akive.api.auth.domain.token.RefreshTokenPayload;
import net.akive.api.common.exception.business.AuthenticationException;

public interface RefreshTokenParser {

    RefreshTokenPayload parse(RefreshToken refreshToken) throws AuthenticationException;
}
