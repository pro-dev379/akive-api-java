package net.akive.api.email.domain;

import java.util.List;

public class EmailMessageFixture {

    public static final String FROM = "from@test.com";
    public static final List<String> TO = List.of("to@test.com");
    public static final String SUBJECT = "subject";
    public static final String CONTENT = "content";

    public static EmailMessage createEmailMessage() {
        return new EmailMessage(
                FROM,
                TO,
                SUBJECT,
                CONTENT
        );
    }
}
