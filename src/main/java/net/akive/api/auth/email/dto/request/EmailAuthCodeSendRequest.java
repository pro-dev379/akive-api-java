package net.akive.api.auth.email.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.auth.email.domain.EmailAuthPurpose;

@Getter
@RequiredArgsConstructor
public class EmailAuthCodeSendRequest {

    @Email
    @NotBlank
    private String email;

    @NotNull
    private EmailAuthPurpose authPurpose;
}
