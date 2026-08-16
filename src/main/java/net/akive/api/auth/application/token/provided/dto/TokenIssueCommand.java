package net.akive.api.auth.application.token.provided.dto;

import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.user.domain.RoleType;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record TokenIssueCommand(Long userId, ClientInstanceId clientInstanceId, RoleType roleType) {

    public TokenIssueCommand {
        requireNonNull(userId);
        requireNonNull(clientInstanceId);
        requireNonNull(roleType);
    }
}
