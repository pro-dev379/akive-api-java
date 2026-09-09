package net.akive.api.auth.domain.login;

import net.akive.api.user.domain.User;

public class LegacyUserPasswordFixture {

    public static final String LEGACY_PASSWORD_HASH =
            "zEkAbzXJyNtGVAJo/EfF1VesO+2IkCGA2datZo2Qk6qzA9VDMDInxZNwPzmkY0Kl1eoSp57EYGuNiiTgjG3qjA==";
    public static final String LEGACY_PASSWORD_SALT = "legacy-password-salt";

    public static LegacyUserPassword createLegacyUserPassword(User user) {
        return new LegacyUserPassword(user, createLegacyPasswordHash());
    }

    public static LegacyUserPassword createLegacyUserPassword(User user, String hash, String salt) {
        return new LegacyUserPassword(user, createLegacyPasswordHash(hash, salt));
    }

    public static LegacyPasswordHash createLegacyPasswordHash() {
        return createLegacyPasswordHash(LEGACY_PASSWORD_HASH, LEGACY_PASSWORD_SALT);
    }

    public static LegacyPasswordHash createLegacyPasswordHash(String hash, String salt) {
        return new LegacyPasswordHash(hash, salt);
    }
}
