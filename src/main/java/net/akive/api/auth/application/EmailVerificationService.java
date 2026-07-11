package net.akive.api.auth.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.dto.result.emailverification.SendEmailVerificationResult;
import net.akive.api.auth.application.dto.command.emailverification.SendEmailVerificationCodeCommand;
import net.akive.api.auth.application.dto.command.emailverification.VerifyEmailVerificationCodeCommand;
import net.akive.api.auth.application.dto.command.emailverification.EnsureEmailVerifiedCommand;
import net.akive.api.auth.application.port.in.emailverification.SendEmailVerificationCodeUseCase;
import net.akive.api.auth.application.port.in.emailverification.EnsureEmailVerifiedUseCase;
import net.akive.api.auth.application.port.in.emailverification.VerifyEmailVerificationCodeUseCase;
import net.akive.api.auth.application.port.out.emailverification.*;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.*;

@Service
@RequiredArgsConstructor
class EmailVerificationService implements
        SendEmailVerificationCodeUseCase,
        VerifyEmailVerificationCodeUseCase,
        EnsureEmailVerifiedUseCase {

    private final SendEmailVerificationMailPort sendEmailVerificationMailPort;
    private final SaveEmailVerificationPort saveEmailVerificationPort;
    private final EmailVerificationDomainService emailVerificationDomainService;

    @Override
    public SendEmailVerificationResult send(SendEmailVerificationCodeCommand command) {
        EmailVerification emailVerification = emailVerificationDomainService.issue(command.email(), command.verificationPurpose());

        sendEmailVerificationMailPort.send(emailVerification, EXPIRE_DURATION);
        saveEmailVerificationPort.save(emailVerification, EXPIRE_DURATION);

        return SendEmailVerificationResult.of(
                command.email(),
                emailVerification.getExpiresAt(),
                EXPIRE_DURATION.toSeconds(),
                RESEND_INTERVAL.toSeconds()
        );
    }

    @Override
    public void verify(VerifyEmailVerificationCodeCommand command) {
        EmailVerification emailVerification = emailVerificationDomainService.verify(
                command.email(),
                command.verificationPurpose(),
                command.verificationCode()
        );

        saveEmailVerificationPort.save(emailVerification, VERIFIED_EXPIRATION_DURATION);
    }

    @Override
    @Transactional(readOnly = true)
    public void ensureVerified(EnsureEmailVerifiedCommand command) {
        emailVerificationDomainService.ensureVerified(command.email(), command.verificationPurpose());
    }
}
