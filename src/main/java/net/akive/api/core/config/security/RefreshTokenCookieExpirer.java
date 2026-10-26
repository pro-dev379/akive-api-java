package net.akive.api.core.config.security;

import org.springframework.http.ResponseCookie;

public interface RefreshTokenCookieExpirer {

    ResponseCookie expire();
}
