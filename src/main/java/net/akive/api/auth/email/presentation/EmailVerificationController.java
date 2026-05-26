package net.akive.api.auth.email.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.akive.api.auth.email.application.dto.EmailVerificationCodeSendResult;
import net.akive.api.auth.email.application.port.in.SendEmailVerificationCodeUseCase;
import net.akive.api.auth.email.application.port.in.VerifyEmailVerificationCodeUseCase;
import net.akive.api.auth.email.presentation.dto.request.EmailVerificationCodeSendRequest;
import net.akive.api.auth.email.presentation.dto.request.EmailVerificationCodeVerifyRequest;
import net.akive.api.auth.email.presentation.dto.response.EmailVerificationCodeSendResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/emails/verification-codes")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final SendEmailVerificationCodeUseCase sendEmailVerificationCodeUseCase;
    private final VerifyEmailVerificationCodeUseCase verifyEmailVerificationCodeUseCase;

    @PostMapping
    public EmailVerificationCodeSendResponse send(@RequestBody @Valid EmailVerificationCodeSendRequest request) {
        EmailVerificationCodeSendResult result = sendEmailVerificationCodeUseCase.send(request.email(), request.verificationPurpose());
        return EmailVerificationCodeSendResponse.from(result);
    }

    @PostMapping("/verifications")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verify(@RequestBody @Valid EmailVerificationCodeVerifyRequest request) {
        verifyEmailVerificationCodeUseCase.verify(request.email(), request.verificationPurpose(), request.verificationCode());
    }
}
