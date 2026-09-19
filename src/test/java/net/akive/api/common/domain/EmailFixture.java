package net.akive.api.common.domain;

public class EmailFixture {

    private EmailFixture() {
    }

    public static final String EMAIL_ADDRESS = "test@test.com";

    public static Email createEmail() {
        return createEmail(EMAIL_ADDRESS);
    }

    public static Email createEmail(String address) {
        return new Email(address);
    }
}
