package net.akive.api.auth.domain.login;

public class LegacyUserPasswordFixture {

    public static final String RAW_PASSWORD = "password123!";
    public static final String LEGACY_PASSWORD_HASH =
            "zEkAbzXJyNtGVAJo/EfF1VesO+2IkCGA2datZo2Qk6qzA9VDMDInxZNwPzmkY0Kl1eoSp57EYGuNiiTgjG3qjA==";
    public static final String LEGACY_PASSWORD_SALT = "legacy-password-salt";

    public static LegacyUserPassword createLegacyUserPassword(Long userId) {
        return new LegacyUserPassword(userId, createLegacyPasswordHash());
    }

    public static LegacyUserPassword createLegacyUserPassword(Long userId, String hash, String salt) {
        return new LegacyUserPassword(userId, createLegacyPasswordHash(hash, salt));
    }

    public static LegacyPasswordHash createLegacyPasswordHash() {
        return createLegacyPasswordHash(LEGACY_PASSWORD_HASH, LEGACY_PASSWORD_SALT);
    }

    public static LegacyPasswordHash createLegacyPasswordHash(String hash, String salt) {
        return new LegacyPasswordHash(hash, salt);
    }
}
