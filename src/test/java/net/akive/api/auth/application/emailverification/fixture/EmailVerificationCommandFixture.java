package net.akive.api.auth.application.emailverification.fixture;

import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationCodeVerifyCommand;
import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationEnsureCommand;
import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationIssueCommand;

import static net.akive.api.auth.domain.emailverification.EmailVerificationFixture.VERIFICATION_CODE;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.user.domain.fixture.UserFixture.EMAIL;

public class EmailVerificationCommandFixture {

    public static EmailVerificationIssueCommand createEmailVerificationIssueCommand() {
        return new EmailVerificationIssueCommand(EMAIL, SIGN_UP);
    }

    public static EmailVerificationCodeVerifyCommand createEmailVerificationCodeVerifyCommand() {
        return new EmailVerificationCodeVerifyCommand(EMAIL, SIGN_UP, VERIFICATION_CODE);
    }

    public static EmailVerificationEnsureCommand createEmailVerificationEnsureCommand() {
        return new EmailVerificationEnsureCommand(EMAIL, SIGN_UP);
    }
}
