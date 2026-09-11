package net.akive.api.auth.adapter.emailverification.webapi.fixture;

import net.akive.api.auth.adapter.emailverification.webapi.dto.request.EmailVerificationCodeIssueRequest;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;

import java.util.stream.Stream;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.common.domain.EmailFixture.EMAIL_ADDRESS;

public class EmailVerificationCodeIssueRequestFixture {

    public static EmailVerificationCodeIssueRequest createIssueRequest() {
        return createIssueRequest(EMAIL_ADDRESS, SIGN_UP);
    }

    public static EmailVerificationCodeIssueRequest createIssueRequest(EmailVerificationPurpose purpose) {
        return createIssueRequest(EMAIL_ADDRESS, purpose);
    }

    public static EmailVerificationCodeIssueRequest createIssueRequest(String emailAddress) {
        return createIssueRequest(emailAddress, SIGN_UP);
    }

    public static EmailVerificationCodeIssueRequest createIssueRequest(
            String emailAddress,
            EmailVerificationPurpose purpose
    ) {
        return new EmailVerificationCodeIssueRequest(emailAddress, purpose);
    }

    public static Stream<EmailVerificationCodeIssueRequest> invalidCodeIssueRequests() {
        return Stream.of(
                createIssueRequest("", SIGN_UP),
                createIssueRequest("invalid-email", SIGN_UP),
                createIssueRequest(EMAIL_ADDRESS, null)
        );
    }
}
