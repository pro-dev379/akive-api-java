package net.akive.api.auth.email.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailVerificationPolicy {

    public final static Duration EXPIRE_DURATION = Duration.ofMinutes(5);
    public final static Duration RESEND_INTERVAL = Duration.ofSeconds(30);
    public final static int MAX_RESEND_COUNT = 3;
}
