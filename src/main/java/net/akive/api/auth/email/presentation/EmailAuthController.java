package net.akive.api.auth.email.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.akive.api.auth.email.application.dto.EmailAuthCodeSendResult;
import net.akive.api.auth.email.application.port.in.SendEmailAuthCodeUseCase;
import net.akive.api.auth.email.presentation.dto.request.EmailAuthCodeSendRequest;
import net.akive.api.auth.email.presentation.dto.response.EmailAuthCodeSendResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/email")
@RequiredArgsConstructor
public class EmailAuthController {

    private final SendEmailAuthCodeUseCase sendEmailAuthCodeUseCase;

    @PostMapping("/codes")
    public EmailAuthCodeSendResponse sendAuthCode(@RequestBody @Valid EmailAuthCodeSendRequest request) {
        EmailAuthCodeSendResult result = sendEmailAuthCodeUseCase.sendAuthCode(request.email(), request.authPurpose());
        return EmailAuthCodeSendResponse.from(result);
    }
}
