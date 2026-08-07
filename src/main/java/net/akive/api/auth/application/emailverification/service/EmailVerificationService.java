package net.akive.api.auth.application.emailverification.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.emailverification.provided.dto.SendEmailVerificationResult;
import net.akive.api.auth.application.emailverification.provided.dto.SendEmailVerificationCommand;
import net.akive.api.auth.application.emailverification.provided.dto.VerifyEmailVerificationCodeCommand;
import net.akive.api.auth.application.emailverification.provided.dto.EnsureEmailVerifiedCommand;
import net.akive.api.auth.application.emailverification.provided.SendEmailVerificationCodeUseCase;
import net.akive.api.auth.application.emailverification.provided.EnsureEmailVerifiedUseCase;
import net.akive.api.auth.application.emailverification.provided.VerifyEmailVerificationCodeUseCase;
import net.akive.api.auth.application.emailverification.required.SaveEmailVerificationPort;
import net.akive.api.auth.application.emailverification.required.SendEmailVerificationMailPort;
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
