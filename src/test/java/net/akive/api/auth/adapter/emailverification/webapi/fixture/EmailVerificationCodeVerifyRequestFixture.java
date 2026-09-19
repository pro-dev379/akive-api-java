package net.akive.api.auth.adapter.emailverification.webapi.fixture;

import net.akive.api.auth.adapter.emailverification.webapi.dto.request.EmailVerificationCodeVerifyRequest;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;

import java.util.stream.Stream;

import static net.akive.api.auth.domain.emailverification.EmailVerificationFixture.VERIFICATION_CODE;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.common.domain.EmailFixture.EMAIL_ADDRESS;

public class EmailVerificationCodeVerifyRequestFixture {

    private EmailVerificationCodeVerifyRequestFixture() {
    }

    public static EmailVerificationCodeVerifyRequest createVerifyRequest() {
        return createVerifyRequest(EMAIL_ADDRESS, SIGN_UP, VERIFICATION_CODE.value());
    }

    public static EmailVerificationCodeVerifyRequest createVerifyRequest(String verificationCode) {
        return createVerifyRequest(EMAIL_ADDRESS, SIGN_UP, verificationCode);
    }

    public static EmailVerificationCodeVerifyRequest createVerifyRequest(
            String emailAddress,
            EmailVerificationPurpose purpose,
            String verificationCode
    ) {
        return new EmailVerificationCodeVerifyRequest(emailAddress, purpose, verificationCode);
    }

    public static Stream<EmailVerificationCodeVerifyRequest> invalidCodeVerifyRequests() {
        return Stream.of(
                createVerifyRequest("", SIGN_UP, VERIFICATION_CODE.value()),
                createVerifyRequest("invalid-email", SIGN_UP, VERIFICATION_CODE.value()),
                createVerifyRequest(EMAIL_ADDRESS, SIGN_UP, ""),
                createVerifyRequest(EMAIL_ADDRESS, SIGN_UP, "12345"),
                createVerifyRequest(EMAIL_ADDRESS, null, VERIFICATION_CODE.value())
        );
    }
}
