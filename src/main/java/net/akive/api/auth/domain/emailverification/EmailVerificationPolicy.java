package net.akive.api.auth.domain.emailverification;

import lombok.NoArgsConstructor;

import java.time.Duration;

import static lombok.AccessLevel.*;

@NoArgsConstructor(access = PRIVATE)
public class EmailVerificationPolicy {

    public static final Duration EXPIRE_DURATION = Duration.ofMinutes(5);
    public static final Duration RESEND_INTERVAL = Duration.ofSeconds(30);
    public static final int MAX_RESEND_COUNT = 3;
}
