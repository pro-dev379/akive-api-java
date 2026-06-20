package net.akive.api.common.cache;

import lombok.NoArgsConstructor;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;

import static lombok.AccessLevel.*;

@NoArgsConstructor(access = PRIVATE)
public class CacheKeyProvider {

    public static String emailVerification(String email, EmailVerificationPurpose verificationPurpose) {
        return "email-auth:%s:%s".formatted(email, verificationPurpose.getCode());
    }

    public static String refreshToken(Long userId) {
        return "refresh-token:%s".formatted(userId);
    }
}
