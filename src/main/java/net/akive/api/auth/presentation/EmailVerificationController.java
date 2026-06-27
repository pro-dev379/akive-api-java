package net.akive.api.auth.presentation;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.dto.result.EmailVerificationCodeSendResult;
import net.akive.api.auth.application.port.in.emailverification.SendEmailVerificationCodeUseCase;
import net.akive.api.auth.application.port.in.emailverification.VerifyEmailVerificationCodeUseCase;
import net.akive.api.auth.presentation.dto.request.EmailVerificationCodeSendRequest;
import net.akive.api.auth.presentation.dto.request.EmailVerificationCodeVerifyRequest;
import net.akive.api.auth.presentation.dto.response.EmailVerificationCodeSendResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/emails/verification-codes")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final SendEmailVerificationCodeUseCase sendEmailVerificationCodeUseCase;
    private final VerifyEmailVerificationCodeUseCase verifyEmailVerificationCodeUseCase;

    @PostMapping
    public EmailVerificationCodeSendResponse send(@RequestBody @Validated EmailVerificationCodeSendRequest request) {
        EmailVerificationCodeSendResult result = sendEmailVerificationCodeUseCase.send(request.toCommand());
        return EmailVerificationCodeSendResponse.from(result);
    }

    @PostMapping("/verifications")
    @ResponseStatus(NO_CONTENT)
    public void verify(@RequestBody @Validated EmailVerificationCodeVerifyRequest request) {
        verifyEmailVerificationCodeUseCase.verify(request.toCommand());
    }
}
