package net.akive.api.auth.application.token.provided.dto;

import net.akive.api.auth.domain.token.RefreshToken;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record TokenReissueCommand(RefreshToken refreshToken) {

    public TokenReissueCommand {
        requireNonNull(refreshToken);
    }
}
