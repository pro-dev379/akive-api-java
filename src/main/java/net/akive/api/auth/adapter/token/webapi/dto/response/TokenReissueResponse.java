package net.akive.api.auth.adapter.token.webapi.dto.response;

import net.akive.api.auth.domain.token.ReissuedToken;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record TokenReissueResponse(String accessToken) {

    public static TokenReissueResponse from(ReissuedToken token) {
        requireNonNull(token);

        return new TokenReissueResponse(token.accessToken().value());
    }
}
