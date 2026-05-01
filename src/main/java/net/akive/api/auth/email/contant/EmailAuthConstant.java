package net.akive.api.auth.email.contant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailAuthConstant {
    public final static int EXPIRE_MINUTES = 5;
    public final static int MAX_RESEND_COUNT = 3;
    public final static int RESEND_AFTER_SECONDS = 30;
}
