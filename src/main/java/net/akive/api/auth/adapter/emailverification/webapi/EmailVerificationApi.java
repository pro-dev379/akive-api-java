package net.akive.api.auth.adapter.emailverification.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.adapter.emailverification.webapi.dto.request.EmailVerificationCodeIssueRequest;
import net.akive.api.auth.adapter.emailverification.webapi.dto.request.EmailVerificationCodeVerifyRequest;
import net.akive.api.auth.adapter.emailverification.webapi.dto.response.EmailVerificationCodeIssueResponse;
import net.akive.api.auth.application.emailverification.provided.EmailVerificationCodeIssuer;
import net.akive.api.auth.application.emailverification.provided.EmailVerificationCodeVerifier;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/verifications/emails")
@RequiredArgsConstructor
public class EmailVerificationApi {

    private final EmailVerificationCodeIssuer emailVerificationCodeIssuer;
    private final EmailVerificationCodeVerifier emailVerificationCodeVerifier;

    @PostMapping
    @ResponseStatus(NO_CONTENT)
    public void verify(@RequestBody @Validated EmailVerificationCodeVerifyRequest request) {
        emailVerificationCodeVerifier.verify(request.toCommand());
    }

    @PostMapping("/codes")
    public EmailVerificationCodeIssueResponse issue(@RequestBody @Validated EmailVerificationCodeIssueRequest request) {
        EmailVerification emailVerification = emailVerificationCodeIssuer.issue(request.toCommand());

        return EmailVerificationCodeIssueResponse.fromResult(emailVerification);
    }
}
