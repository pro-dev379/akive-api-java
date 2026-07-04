package net.akive.api.auth.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.dto.result.emailverification.EmailVerificationCodeSendResult;
import net.akive.api.auth.application.dto.command.emailverification.EmailVerificationCodeSendCommand;
import net.akive.api.auth.application.dto.command.emailverification.EmailVerificationCodeVerifyCommand;
import net.akive.api.auth.application.dto.command.emailverification.EmailVerificationEnsureVerifiedCommand;
import net.akive.api.auth.application.port.in.emailverification.SendEmailVerificationCodeUseCase;
import net.akive.api.auth.application.port.in.emailverification.EnsureEmailVerifiedUseCase;
import net.akive.api.auth.application.port.in.emailverification.VerifyEmailVerificationCodeUseCase;
import net.akive.api.auth.application.port.out.emailverification.*;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.auth.domain.emailverification.exception.EmailVerificationException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.user.application.port.in.ValidateUserUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.*;
import static net.akive.api.auth.domain.emailverification.exception.EmailVerificationErrorCode.*;

@Service
@RequiredArgsConstructor
class EmailVerificationService
        implements SendEmailVerificationCodeUseCase,
                   VerifyEmailVerificationCodeUseCase,
                   EnsureEmailVerifiedUseCase {

    private final ValidateUserUseCase validateUserUseCase;

    private final SendEmailVerificationMailPort sendEmailVerificationMailPort;
    private final GenerateEmailVerificationCodePort generateEmailVerificationCodePort;
    private final LoadEmailVerificationPort loadEmailVerificationPort;
    private final SaveEmailVerificationPort saveEmailVerificationPort;

    private final CurrentTimeProvider currentTimeProvider;

    @Override
    public EmailVerificationCodeSendResult send(EmailVerificationCodeSendCommand command) {
        validateEmailByPurpose(command.email(), command.verificationPurpose());

        LocalDateTime requestedAt = currentTimeProvider.now();
        String verificationCode = generateEmailVerificationCodePort.generate();

        EmailVerification emailVerification = loadEmailVerificationPort
                .loadByEmailAndVerificationPurpose(command.email(), command.verificationPurpose())
                .map(previous -> {
                    previous.renew(verificationCode, requestedAt);
                    return previous;
                })
                .orElseGet(() -> EmailVerification.create(command.email(), command.verificationPurpose(), verificationCode, requestedAt));

        sendEmailVerificationMailPort.send(command.email(), verificationCode, EXPIRE_DURATION);
        saveEmailVerificationPort.save(emailVerification, EXPIRE_DURATION);

        return EmailVerificationCodeSendResult.of(
                command.email(),
                emailVerification.getExpiredAt(),
                EXPIRE_DURATION.toSeconds(),
                RESEND_INTERVAL.toSeconds()
        );
    }

    @Override
    public void verify(EmailVerificationCodeVerifyCommand command) {
        EmailVerification emailVerification = loadEmailVerificationPort
                .loadByEmailAndVerificationPurpose(command.email(), command.verificationPurpose())
                .orElseThrow(() -> new EmailVerificationException(NOT_FOUND));

        emailVerification.verify(command.verificationCode(), currentTimeProvider.now());

        saveEmailVerificationPort.save(emailVerification, VERIFIED_EXPIRATION_DURATION);
    }

    @Override
    @Transactional(readOnly = true)
    public void ensureVerified(EmailVerificationEnsureVerifiedCommand command) {
        EmailVerification emailVerification = loadEmailVerificationPort
                .loadByEmailAndVerificationPurpose(command.email(), command.verificationPurpose())
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
