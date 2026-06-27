package net.akive.api.auth.domain.login;

public record LegacyUserPassword(
        Long userId,
        String hash,
        String salt
) {
}