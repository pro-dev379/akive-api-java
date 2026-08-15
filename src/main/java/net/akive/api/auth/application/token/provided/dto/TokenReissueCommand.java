package net.akive.api.auth.application.token.provided.dto;

import net.akive.api.auth.domain.token.RefreshToken;

public record TokenReissueCommand(RefreshToken refreshToken) {
}
