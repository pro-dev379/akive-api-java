package net.akive.api.common.cache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.akive.api.auth.email.domain.EmailAuthPurpose;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheKeyProvider {

    public static String emailAuth(String email, EmailAuthPurpose authPurpose) {
        return "email-auth:%s:%s".formatted(email, authPurpose.getCode());
    }
}
