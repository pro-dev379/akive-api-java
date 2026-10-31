package net.akive.api.user.adapter.webapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import net.akive.api.user.application.provided.dto.SignUpLegalDocumentActionCommand;
import net.akive.api.user.application.provided.dto.UserSignUpByEmailCommand;
import net.akive.api.user.domain.SignUpLegalDocumentActionType;

import java.util.List;
import java.util.UUID;

import static net.akive.api.user.domain.UserPolicy.NICKNAME_MAX_LENGTH;
import static net.akive.api.user.domain.UserPolicy.PASSWORD_MIN_LENGTH;
import static net.akive.api.user.domain.UserPolicy.PASSWORD_PATTERN_REGEX;

public record UserSignUpByEmailRequest(
        @Email
        @NotBlank
        String email,

        @NotBlank
        @Size(min = PASSWORD_MIN_LENGTH)
        @Pattern(regexp = PASSWORD_PATTERN_REGEX)
        String password,

        @Size(max = NICKNAME_MAX_LENGTH)
        String nickname,

        @NotEmpty
        List<@NotNull @Valid LegalDocumentActionRequest> legalDocumentActions
) {

    public UserSignUpByEmailCommand to() {
        return UserSignUpByEmailCommand.of(
                this.email,
                this.password,
                this.nickname,
                this.legalDocumentActions.stream()
                        .map(LegalDocumentActionRequest::to)
                        .toList()
        );
    }

    public record LegalDocumentActionRequest(
            @NotNull UUID requestId,
            @NotNull @Positive Long legalDocumentId,
            @NotNull SignUpLegalDocumentActionType actionType
    ) {

        private SignUpLegalDocumentActionCommand to() {
            return new SignUpLegalDocumentActionCommand(this.requestId, this.legalDocumentId, this.actionType);
        }
    }
}
