package net.akive.api.auth.application.port.in.token;

import net.akive.api.auth.application.dto.result.token.TokenIssueResult;
import net.akive.api.auth.application.dto.result.token.TokenReissueResult;
import net.akive.api.user.domain.RoleType;

public interface IssueTokenUseCase {

    TokenIssueResult issue(Long userId, RoleType roleType);
    TokenReissueResult reissue(String refreshToken);
}
