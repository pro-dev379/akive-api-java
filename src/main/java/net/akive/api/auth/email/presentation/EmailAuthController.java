package net.akive.api.auth.email.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.akive.api.auth.email.application.dto.EmailAuthCodeSendResult;
import net.akive.api.auth.email.application.port.in.SendEmailAuthCodeUseCase;
import net.akive.api.auth.email.application.port.in.VerifyEmailAuthCodeUseCase;
import net.akive.api.auth.email.presentation.dto.request.EmailAuthCodeSendRequest;
import net.akive.api.auth.email.presentation.dto.request.EmailAuthCodeVerifyRequest;
import net.akive.api.auth.email.presentation.dto.response.EmailAuthCodeSendResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/email")
@RequiredArgsConstructor
public class EmailAuthController {

    private final SendEmailAuthCodeUseCase sendEmailAuthCodeUseCase;
    private final VerifyEmailAuthCodeUseCase verifyEmailAuthCodeUseCase;

    @PostMapping("/codes")
    public EmailAuthCodeSendResponse sendAuthCode(@RequestBody @Valid EmailAuthCodeSendRequest request) {
        EmailAuthCodeSendResult result = sendEmailAuthCodeUseCase.sendAuthCode(request.email(), request.authPurpose());
        return EmailAuthCodeSendResponse.from(result);
    }

    @PostMapping("/verifications")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyAuthCode(@RequestBody @Valid EmailAuthCodeVerifyRequest request) {
        verifyEmailAuthCodeUseCase.verifyAuthCode(request.email(), request.authPurpose(), request.authCode());
    }
}
