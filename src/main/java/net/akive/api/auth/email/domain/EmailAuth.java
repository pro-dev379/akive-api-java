package net.akive.api.auth.email.domain;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailAuth {

    private String authCode;
    private int resendCount;
    private boolean verified;
    private LocalDateTime expiredAt;
    private LocalDateTime lastSentAt;
    private LocalDateTime createdAt;

    public static EmailAuth create(String authCode, int expireMinutes) {
        LocalDateTime now = LocalDateTime.now();
        return new EmailAuth(
                authCode,
                0,
                false,
                now.plusMinutes(expireMinutes),
                now,
                now
        );
    }

    public static EmailAuth renew(EmailAuth previousEmailAuth, String authCode, int expireMinutes) {
        LocalDateTime now = LocalDateTime.now();
        return new EmailAuth(
                authCode,
                previousEmailAuth.getResendCount() + 1,
                false,
                now.plusMinutes(expireMinutes),
                now,
                previousEmailAuth.getCreatedAt()
        );
    }
}
