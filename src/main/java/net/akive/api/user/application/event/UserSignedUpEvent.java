package net.akive.api.user.application.event;

import net.akive.api.common.domain.Email;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record UserSignedUpEvent(Long userId, Email email, String nickname) {

    public UserSignedUpEvent {
        requirePositive(userId);
        requireNonNull(email);
        nickname = requireNotBlank(nickname);
    }
}
