package net.akive.api.auth.email.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.email.application.dto.EmailVerificationCodeSendResult;
import net.akive.api.auth.email.application.port.in.SendEmailVerificationCodeUseCase;
import net.akive.api.auth.email.application.port.in.VerifyEmailVerificationUseCase;
import net.akive.api.auth.email.application.port.out.EmailVerificationCodeGenerator;
import net.akive.api.auth.email.application.port.out.EmailVerificationMailSender;
import net.akive.api.auth.email.application.port.out.EmailVerificationRepository;
import net.akive.api.auth.email.application.port.out.EmailVerificationUserValidator;
import net.akive.api.auth.email.domain.EmailVerification;
import net.akive.api.auth.email.domain.EmailVerificationPurpose;
import net.akive.api.auth.email.domain.exception.EmailAuthNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import static net.akive.api.auth.email.domain.EmailVerificationPolicy.*;

@Service
@RequiredArgsConstructor
public class EmailVerificationService implements SendEmailVerificationCodeUseCase, VerifyEmailVerificationUseCase {

    private final EmailVerificationMailSender emailVerificationMailSender;
    private final EmailVerificationCodeGenerator emailVerificationCodeGenerator;
    private final EmailVerificationUserValidator emailVerificationUserValidator;
    private final EmailVerificationRepository emailVerificationRepository;
    private final Clock clock;

    @Override
    public EmailVerificationCodeSendResult send(String email, EmailVerificationPurpose verificationPurpose) {
        validateEmailByPurpose(email, verificationPurpose);

        LocalDateTime requestedAt = LocalDateTime.now(clock);
        String verificationCode = emailVerificationCodeGenerator.generate();

        EmailVerification emailVerification = emailVerificationRepository
                .findByEmailAndVerificationPurpose(email, verificationPurpose)
                .map(previous -> {
                    previous.renew(verificationCode, requestedAt);
                    return previous;
                })
                .orElseGet(() -> EmailVerification.create(email, verificationPurpose, verificationCode, requestedAt));

        emailVerificationMailSender.send(email, verificationCode, EXPIRE_DURATION);
        emailVerificationRepository.save(emailVerification, EXPIRE_DURATION);

        return EmailVerificationCodeSendResult.of(
                email,
                emailVerification.getExpiredAt(),
                EXPIRE_DURATION.toSeconds(),
                RESEND_INTERVAL.toSeconds()
        );
    }

    @Override
    public void verify(String email, EmailVerificationPurpose verificationPurpose, String verificationCode) {
        EmailVerification emailVerification = emailVerificationRepository
                .findByEmailAndVerificationPurpose(email, verificationPurpose)
                .orElseThrow(EmailAuthNotFoundException::new);

        emailVerification.verify(verificationCode, LocalDateTime.now(clock));

        emailVerificationRepository.save(emailVerification, Duration.ofMinutes(15));
    }

    private void validateEmailByPurpose(String email, EmailVerificationPurpose verificationPurpose) {
        switch (verificationPurpose) {
            case FIND_PASSWORD ->  emailVerificationUserValidator.validateUserExistsByEmail(email);
            case JOIN -> emailVerificationUserValidator.validateEmailNotDuplicate(email);
        }
    }
}
