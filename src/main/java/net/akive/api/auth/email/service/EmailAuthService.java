package net.akive.api.auth.email.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.email.domain.EmailAuth;
import net.akive.api.auth.email.domain.EmailAuthPurpose;
import net.akive.api.auth.email.dto.response.EmailAuthCodeSendResponse;
import net.akive.api.auth.email.exception.EmailAuthResendLimitExceededException;
import net.akive.api.auth.email.exception.EmailAuthResendTooSoonException;
import net.akive.api.auth.email.repository.EmailAuthCacheRepository;
import net.akive.api.common.email.EmailService;
import net.akive.api.user.service.UserValidationService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static net.akive.api.auth.email.contant.EmailAuthConstant.*;

@Service
@RequiredArgsConstructor
public class EmailAuthService {

    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final UserValidationService userValidationService;
    private final EmailAuthCacheRepository emailAuthCacheRepository;

    public EmailAuthCodeSendResponse sendAuthCode(String email, EmailAuthPurpose authPurpose) {
        validateEmailByPurpose(email, authPurpose);

        String authCode = createAuthCode();
        EmailAuth emailAuth = emailAuthCacheRepository
                .findByPurposeAndEmail(authPurpose, email)
                .map(previousEmailAuth -> {
                    validateResendAvailable(previousEmailAuth);
                    return EmailAuth.renew(previousEmailAuth, authCode, EXPIRE_MINUTES);
                })
                .orElseGet(() -> EmailAuth.create(authCode, EXPIRE_MINUTES));

        emailAuthCacheRepository.save(authPurpose, email, emailAuth, Duration.ofMinutes(EXPIRE_MINUTES));

        emailService.sendTemplateHtml(
                List.of(email),
                "[AKIVE] 인증 코드가 도착했어요.",
                "email/auth-code",
                Map.of(
                    "authCode", authCode,
                    "expireMin", EXPIRE_MINUTES
                )
        );

        return EmailAuthCodeSendResponse.of(
                email,
                emailAuth.getExpiredAt(),
                EXPIRE_MINUTES * 60,
                RESEND_AFTER_SECONDS
        );
    }

    private void validateEmailByPurpose(String email, EmailAuthPurpose authPurpose) {
        switch (authPurpose) {
            case FIND_PASSWORD ->  userValidationService.validateUserExistsByEmail(email);
            case JOIN -> userValidationService.validateEmailNotDuplicate(email);
        }
    }

    private String createAuthCode() {
        int authCode = secureRandom.nextInt(1_000_000);
        return String.format("%06d", authCode);
    }

    private void validateResendAvailable(EmailAuth previousEmailAuth) {
        if (previousEmailAuth.getResendCount() >= MAX_RESEND_COUNT) {
            throw new EmailAuthResendLimitExceededException();
        }

        long elapsedSeconds = Duration.between(
                previousEmailAuth.getLastSentAt(),
                LocalDateTime.now()
        ).toSeconds();

        if (elapsedSeconds <= RESEND_AFTER_SECONDS) {
            throw new EmailAuthResendTooSoonException();
        }
    }
}
