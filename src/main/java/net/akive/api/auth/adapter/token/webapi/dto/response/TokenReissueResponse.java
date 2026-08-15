package net.akive.api.auth.adapter.token.webapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.akive.api.auth.domain.token.ReissuedToken;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static net.akive.api.common.domain.DomainValidator.requireNonNull;

@JsonInclude(NON_NULL)
public record TokenReissueResponse(String accessToken, String refreshToken) {

    public static TokenReissueResponse from(ReissuedToken token) {
        requireNonNull(token);

        return new TokenReissueResponse(
                token.accessToken().value(),
                token.rotatedRefreshToken() != null ? token.rotatedRefreshToken().value() : null
        );
    }
}
