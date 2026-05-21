package net.akive.api.common.email;

import java.util.List;

public record EmailMessage(
        String from,
        List<String> to,
        String subject,
        String content
) {

    private final static String DEFAULT_FROM = "akive@akive.net";

    public static EmailMessage of(
            List<String> to,
            String subject,
            String content
    ) {
        return new EmailMessage(DEFAULT_FROM, to, subject, content);
    }
}
