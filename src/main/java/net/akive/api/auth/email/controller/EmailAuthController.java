package net.akive.api.auth.email.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.akive.api.auth.email.dto.request.EmailAuthCodeSendRequest;
import net.akive.api.auth.email.dto.response.EmailAuthCodeSendResponse;
import net.akive.api.auth.email.service.EmailAuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/email")
@RequiredArgsConstructor
public class EmailAuthController {

    private final EmailAuthService emailAuthService;

    @PostMapping("/codes")
    public EmailAuthCodeSendResponse sendAuthCode(@RequestBody @Valid EmailAuthCodeSendRequest request) {
        return emailAuthService.sendAuthCode(request.getEmail(), request.getAuthPurpose());
    }
}
