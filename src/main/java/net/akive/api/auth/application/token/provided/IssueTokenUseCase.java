package net.akive.api.auth.application.token.provided;

import net.akive.api.auth.application.token.provided.dto.IssueTokenResult;
import net.akive.api.auth.application.token.provided.dto.ReissueTokenResult;
import net.akive.api.user.domain.RoleType;

public interface IssueTokenUseCase {

    IssueTokenResult issue(Long userId, RoleType roleType);
    ReissueTokenResult reissue(String refreshToken);
}
