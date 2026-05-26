package net.akive.api.common.cache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.akive.api.auth.domain.EmailVerificationPurpose;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheKeyProvider {

    public static String emailVerification(String email, EmailVerificationPurpose verificationPurpose) {
        return "email-auth:%s:%s".formatted(email, verificationPurpose.getCode());
    }
}
