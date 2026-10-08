package net.akive.api.auth.application.token.provided.dto;

import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.user.domain.UserRole;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record TokenIssueCommand(Long userId, ClientInstanceId clientInstanceId, UserRole role) {

    public TokenIssueCommand {
        requireNonNull(userId);
        requireNonNull(clientInstanceId);
        requireNonNull(role);
    }
}
