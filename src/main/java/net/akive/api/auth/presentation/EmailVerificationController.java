package net.akive.api.auth.presentation;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.dto.result.emailverification.SendEmailVerificationResult;
import net.akive.api.auth.application.port.in.emailverification.SendEmailVerificationCodeUseCase;
import net.akive.api.auth.application.port.in.emailverification.VerifyEmailVerificationCodeUseCase;
import net.akive.api.auth.presentation.dto.request.emailverification.SendEmailVerificationCodeRequest;
import net.akive.api.auth.presentation.dto.request.emailverification.VerifyEmailVerificationCodeRequest;
import net.akive.api.auth.presentation.dto.response.SendEmailVerificationCodeResponse;
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
    public SendEmailVerificationCodeResponse send(@RequestBody @Validated SendEmailVerificationCodeRequest request) {
        SendEmailVerificationResult result = sendEmailVerificationCodeUseCase.send(request.to());
        return SendEmailVerificationCodeResponse.from(result);
    }

    @PostMapping("/verifications")
    @ResponseStatus(NO_CONTENT)
    public void verify(@RequestBody @Validated VerifyEmailVerificationCodeRequest request) {
        verifyEmailVerificationCodeUseCase.verify(request.to());
    }
}
