package net.akive.api.auth.adapter.emailverification.webapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationCodeVerifyCommand;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFICATION_CODE_REGEX;

public record EmailVerificationCodeVerifyRequest(
    @Email @NotBlank String email,
    @NotNull EmailVerificationPurpose verificationPurpose,
    @NotBlank @Pattern(regexp = VERIFICATION_CODE_REGEX) String verificationCode
) {

    public EmailVerificationCodeVerifyCommand toCommand() {
        return EmailVerificationCodeVerifyCommand.of(this.email, this.verificationPurpose, this.verificationCode);
    }
}
