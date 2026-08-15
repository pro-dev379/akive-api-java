package net.akive.api.auth.application.token.provided.dto;

import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.user.domain.RoleType;

public record TokenIssueCommand(Long userId, ClientInstanceId clientInstanceId, RoleType roleType) {
}
