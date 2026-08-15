package net.akive.api.auth.domain.emailverification;

import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.regex.Pattern;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class EmailVerificationPolicy {

    public static final int VERIFICATION_CODE_LENGTH = 6;
    public static final int VERIFICATION_CODE_BOUND = 1_000_000;
    public static final String VERIFICATION_CODE_REGEX = "\\d{" + VERIFICATION_CODE_LENGTH + "}";
    public static final Pattern VERIFICATION_CODE_PATTERN = Pattern.compile(VERIFICATION_CODE_REGEX);
    public static final Duration VERIFICATION_CODE_EXPIRATION_DURATION = Duration.ofMinutes(5);
    public static final Duration VERIFIED_EMAIL_VERIFICATION_EXPIRATION_DURATION = Duration.ofMinutes(15);
    public static final Duration EMAIL_VERIFICATION_RENEWAL_MIN_INTERVAL = Duration.ofSeconds(30);
    public static final int EMAIL_VERIFICATION_RENEWAL_MAX_COUNT = 3;
}
