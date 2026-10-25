package net.akive.api.auth.adapter.login.webapi.dto.response;

import net.akive.api.auth.domain.token.IssuedToken;

public record LoginResponse(String accessToken) {

    public static LoginResponse from(IssuedToken token) {
        return new LoginResponse(token.accessToken().value());
    }
}
