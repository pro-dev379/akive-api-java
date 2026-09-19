package net.akive.api.email.application.fixture;

import net.akive.api.email.application.provided.dto.EmailSendCommand;

import java.util.Map;

import static net.akive.api.email.domain.EmailMessageFixture.*;

public class EmailSendCommandFixture {

    private EmailSendCommandFixture() {
    }

    public static final String TEMPLATE_PATH = "email/hello.html";
    public static final Map<String, Object> TEMPLATE_VARIABLES = Map.of("text", "hello");
    public static final String RENDERED_CONTENT = "<h1>hello</h1>";

    public static EmailSendCommand createEmailSendCommand() {
        return new EmailSendCommand(FROM, TO, SUBJECT, TEMPLATE_PATH, TEMPLATE_VARIABLES);
    }
}
