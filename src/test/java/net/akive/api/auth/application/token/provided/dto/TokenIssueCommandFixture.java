package net.akive.api.auth.application.token.provided.dto;

import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.user.domain.UserRole;

import static net.akive.api.common.domain.ClientInstanceIdFixture.createClientInstanceId;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ROLE;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public class TokenIssueCommandFixture {

    private TokenIssueCommandFixture() {
    }

    public static TokenIssueCommand createTokenIssueCommand() {
        return createTokenIssueCommand(USER_ID, createClientInstanceId(), USER_ROLE);
    }

    public static TokenIssueCommand createTokenIssueCommand(
            Long userId,
            ClientInstanceId clientInstanceId,
            UserRole role
    ) {
        return new TokenIssueCommand(userId, clientInstanceId, role);
    }
}
