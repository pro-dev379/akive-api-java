package net.akive.api.auth.application.port.in.token;

import net.akive.api.auth.application.dto.result.token.IssueTokenResult;
import net.akive.api.auth.application.dto.result.token.ReissueTokenResult;
import net.akive.api.user.domain.RoleType;

public interface IssueTokenUseCase {

    IssueTokenResult issue(Long userId, RoleType roleType);
    ReissueTokenResult reissue(String refreshToken);
}
