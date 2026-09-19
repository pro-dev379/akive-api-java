package net.akive.api.auth.application.token.provided.dto;

import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.user.domain.RoleType;

import static net.akive.api.common.domain.ClientInstanceIdFixture.createClientInstanceId;
import static net.akive.api.user.domain.fixture.UserFixture.ROLE_TYPE;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public class TokenIssueCommandFixture {

    private TokenIssueCommandFixture() {
    }

    public static TokenIssueCommand createTokenIssueCommand() {
        return createTokenIssueCommand(USER_ID, createClientInstanceId(), ROLE_TYPE);
    }

    public static TokenIssueCommand createTokenIssueCommand(
            Long userId,
            ClientInstanceId clientInstanceId,
            RoleType roleType
    ) {
        return new TokenIssueCommand(userId, clientInstanceId, roleType);
    }
}
