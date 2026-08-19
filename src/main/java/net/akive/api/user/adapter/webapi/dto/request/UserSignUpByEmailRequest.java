package net.akive.api.user.adapter.webapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static net.akive.api.user.domain.UserPolicy.*;

public record UserSignUpByEmailRequest(
        @Email
        @NotBlank
        String email,

        @NotBlank
        @Size(min = PASSWORD_MIN_LENGTH)
        @Pattern(regexp = PASSWORD_PATTERN_REGEX)
        String password,

        @Size(max = NICKNAME_MAX_LENGTH)
        String nickname
) {
}
