package net.akive.api.auth.application.emailverification.provided;

import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationIssueCommand;
import net.akive.api.auth.domain.emailverification.EmailVerification;

public interface EmailVerificationCodeIssuer {

    EmailVerification issue(EmailVerificationIssueCommand command);
}
