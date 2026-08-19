package net.akive.api.auth.application.token.provided.dto;

import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.user.domain.RoleType;

import static net.akive.api.common.domain.ClientInstanceIdFixture.createClientInstanceId;
import static net.akive.api.user.domain.RoleType.MEMBER;

public class TokenIssueCommandFixture {

    public static TokenIssueCommand createTokenIssueCommand() {
        return createTokenIssueCommand(1L, createClientInstanceId(), MEMBER);
    }

    public static TokenIssueCommand createTokenIssueCommand(
            Long userId,
            ClientInstanceId clientInstanceId,
            RoleType roleType
    ) {
        return new TokenIssueCommand(userId, clientInstanceId, roleType);
    }
}
