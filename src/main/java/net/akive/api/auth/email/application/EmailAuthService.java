package net.akive.api.auth.email.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.email.application.dto.EmailAuthCodeSendResult;
import net.akive.api.auth.email.application.port.in.SendEmailAuthCodeUseCase;
import net.akive.api.auth.email.application.port.in.VerifyEmailAuthCodeUseCase;
import net.akive.api.auth.email.application.port.out.EmailAuthCodeGenerator;
import net.akive.api.auth.email.application.port.out.EmailAuthMailSender;
import net.akive.api.auth.email.application.port.out.EmailAuthRepository;
import net.akive.api.auth.email.application.port.out.EmailAuthUserValidator;
import net.akive.api.auth.email.domain.EmailAuth;
import net.akive.api.auth.email.domain.EmailAuthPurpose;
import net.akive.api.auth.email.domain.exception.EmailAuthNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import static net.akive.api.auth.email.domain.EmailAuthPolicy.*;

@Service
@RequiredArgsConstructor
public class EmailAuthService implements SendEmailAuthCodeUseCase, VerifyEmailAuthCodeUseCase {

    private final EmailAuthMailSender emailAuthMailSender;
    private final EmailAuthCodeGenerator emailAuthCodeGenerator;
    private final EmailAuthUserValidator emailAuthUserValidator;
    private final EmailAuthRepository emailAuthRepository;
    private final Clock clock;

    @Override
    public EmailAuthCodeSendResult sendAuthCode(String email, EmailAuthPurpose authPurpose) {
        validateEmailByPurpose(email, authPurpose);

        LocalDateTime requestedAt = LocalDateTime.now(clock);
        String authCode = emailAuthCodeGenerator.generate();

        EmailAuth emailAuth = emailAuthRepository
                .findByEmailAndPurpose(email, authPurpose)
                .map(previous -> {
                    previous.renew(authCode, requestedAt);
                    return previous;
                })
                .orElseGet(() -> EmailAuth.create(email, authPurpose, authCode, requestedAt));

        emailAuthMailSender.sendAuthCode(email, authCode, EXPIRE_DURATION);
        emailAuthRepository.save(emailAuth, EXPIRE_DURATION);

        return EmailAuthCodeSendResult.of(
                email,
                emailAuth.getExpiredAt(),
                EXPIRE_DURATION.toSeconds(),
                RESEND_INTERVAL.toSeconds()
        );
    }

    @Override
    public void verifyAuthCode(String email, EmailAuthPurpose authPurpose, String authCode) {
        EmailAuth emailAuth = emailAuthRepository
                .findByEmailAndPurpose(email, authPurpose)
                .orElseThrow(EmailAuthNotFoundException::new);

        emailAuth.verify(authCode, LocalDateTime.now(clock));

        emailAuthRepository.save(emailAuth, Duration.ofMinutes(15));
    }

    private void validateEmailByPurpose(String email, EmailAuthPurpose authPurpose) {
        switch (authPurpose) {
            case FIND_PASSWORD ->  emailAuthUserValidator.validateUserExistsByEmail(email);
            case JOIN -> emailAuthUserValidator.validateEmailNotDuplicate(email);
        }
    }
}
