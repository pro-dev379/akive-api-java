package net.akive.api.auth.application.token.provided;

import net.akive.api.auth.application.token.provided.dto.TokenIssueCommand;
import net.akive.api.auth.application.token.provided.dto.TokenReissueCommand;
import net.akive.api.auth.domain.token.IssuedToken;
import net.akive.api.auth.domain.token.ReissuedToken;
import net.akive.api.common.exception.business.UnauthorizedException;

public interface TokenIssuer {

    IssuedToken issue(TokenIssueCommand command);

    ReissuedToken reissue(TokenReissueCommand command) throws UnauthorizedException;
}
