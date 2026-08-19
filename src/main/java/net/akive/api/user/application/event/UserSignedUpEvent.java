package net.akive.api.user.application.event;

import net.akive.api.common.domain.Email;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;

public record UserSignedUpEvent(Email email, String nickname) {

    public UserSignedUpEvent {
        requireNonNull(email);
        nickname = requireNotBlank(nickname);
    }
}
