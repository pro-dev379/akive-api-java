package net.akive.api.auth.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.dto.EmailVerificationCodeSendResult;
import net.akive.api.auth.application.port.in.SendEmailVerificationCodeUseCase;
import net.akive.api.auth.application.port.in.VerifyEmailVerificationCodeUseCase;
import net.akive.api.auth.application.port.out.EmailVerificationCodeGenerator;
import net.akive.api.auth.application.port.out.EmailVerificationMailSender;
import net.akive.api.auth.application.port.out.EmailVerificationRepository;
import net.akive.api.auth.application.port.out.EmailVerificationUserValidator;
import net.akive.api.auth.domain.EmailVerification;
import net.akive.api.auth.domain.EmailVerificationPurpose;
import net.akive.api.auth.domain.exception.EmailVerificationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import static net.akive.api.auth.domain.EmailVerificationPolicy.*;
import static net.akive.api.auth.domain.exception.EmailVerificationErrorCode.*;

@Service
@RequiredArgsConstructor
public class EmailVerificationCodeService implements SendEmailVerificationCodeUseCase, VerifyEmailVerificationCodeUseCase {

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
                .orElseThrow(() -> new EmailVerificationException(NOT_FOUND));

        emailVerification.verify(verificationCode, LocalDateTime.now(clock));

        emailVerificationRepository.save(emailVerification, Duration.ofMinutes(15));
    }

    private void validateEmailByPurpose(String email, EmailVerificationPurpose verificationPurpose) {
        switch (verificationPurpose) {
            case FIND_PASSWORD ->  emailVerificationUserValidator.validateUserExistsByEmail(email);
            case SIGN_UP -> emailVerificationUserValidator.validateEmailNotDuplicate(email);
        }
    }
}
