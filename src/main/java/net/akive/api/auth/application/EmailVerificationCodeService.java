package net.akive.api.auth.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.dto.EmailVerificationCodeSendResult;
import net.akive.api.auth.application.port.in.emailverification.SendEmailVerificationCodeUseCase;
import net.akive.api.auth.application.port.in.emailverification.ValidateEmailVerificationUseCase;
import net.akive.api.auth.application.port.in.emailverification.VerifyEmailVerificationCodeUseCase;
import net.akive.api.auth.application.port.out.emailverification.EmailVerificationCodeGenerator;
import net.akive.api.auth.application.port.out.emailverification.EmailVerificationMailSender;
import net.akive.api.auth.application.port.out.emailverification.EmailVerificationRepository;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.auth.domain.emailverification.exception.EmailVerificationException;
import net.akive.api.user.application.port.in.ValidateUserUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.*;
import static net.akive.api.auth.domain.emailverification.exception.EmailVerificationErrorCode.*;

@Service
@RequiredArgsConstructor
class EmailVerificationCodeService implements SendEmailVerificationCodeUseCase, VerifyEmailVerificationCodeUseCase, ValidateEmailVerificationUseCase {

    private final EmailVerificationMailSender emailVerificationMailSender;
    private final EmailVerificationCodeGenerator emailVerificationCodeGenerator;
    private final EmailVerificationRepository emailVerificationRepository;
    private final Clock clock;

    private final ValidateUserUseCase validateUserUseCase;

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

    @Override
    @Transactional(readOnly = true)
    public void validateVerified(String email, EmailVerificationPurpose verificationPurpose) {
        EmailVerification emailVerification = emailVerificationRepository
                .findByEmailAndVerificationPurpose(email, verificationPurpose)
                .orElseThrow(() -> new EmailVerificationException(NOT_VERIFIED));

        if (!emailVerification.isVerified()) {
            throw new EmailVerificationException(NOT_VERIFIED);
        }
    }

    private void validateEmailByPurpose(String email, EmailVerificationPurpose verificationPurpose) {
        switch (verificationPurpose) {
            case FIND_PASSWORD ->  validateUserUseCase.validateUserExistsByEmail(email);
            case SIGN_UP -> validateUserUseCase.validateEmailNotDuplicate(email);
        }
    }
}
