package net.akive.api.auth.application.service.emailverification;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.dto.result.emailverification.SendEmailVerificationResult;
import net.akive.api.auth.application.dto.command.emailverification.SendEmailVerificationCommand;
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
@Transactional
@RequiredArgsConstructor
class EmailVerificationService implements
        SendEmailVerificationCodeUseCase,
        VerifyEmailVerificationCodeUseCase,
        EnsureEmailVerifiedUseCase {

    private final SendEmailVerificationMailPort sendEmailVerificationMailPort;
    private final SaveEmailVerificationPort saveEmailVerificationPort;
    private final EmailVerificationDomainService emailVerificationDomainService;

    @Override
    public SendEmailVerificationResult send(SendEmailVerificationCommand command) {
        EmailVerification emailVerification = emailVerificationDomainService.issue(command.email(), command.verificationPurpose());

        sendEmailVerificationMailPort.send(emailVerification, VERIFICATION_CODE_EXPIRE_DURATION);
        saveEmailVerificationPort.save(emailVerification, VERIFICATION_CODE_EXPIRE_DURATION);

        return new SendEmailVerificationResult(
                command.email(),
                emailVerification.getExpiresAt(),
                VERIFICATION_CODE_EXPIRE_DURATION.toSeconds(),
                EMAIL_VERIFICATION_RESEND_MIN_INTERVAL.toSeconds()
        );
    }

    @Override
    public void verify(VerifyEmailVerificationCodeCommand command) {
        EmailVerification emailVerification = emailVerificationDomainService.verify(
                command.email(),
                command.verificationPurpose(),
                command.verificationCode()
        );

        saveEmailVerificationPort.save(emailVerification, VERIFIED_EMAIL_VERIFICATION_EXPIRE_DURATION);
    }

    @Override
    @Transactional(readOnly = true)
    public void ensureVerified(EnsureEmailVerifiedCommand command) {
        emailVerificationDomainService.ensureVerified(command.email(), command.verificationPurpose());
    }
}
