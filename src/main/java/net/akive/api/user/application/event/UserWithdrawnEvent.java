package net.akive.api.user.application.event;

import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record UserWithdrawnEvent(Long userId) {

    public UserWithdrawnEvent {
        requirePositive(userId);
    }
}
