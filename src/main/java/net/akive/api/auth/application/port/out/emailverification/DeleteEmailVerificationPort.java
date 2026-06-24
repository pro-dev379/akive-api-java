package net.akive.api.auth.application.port.out.emailverification;

import net.akive.api.auth.domain.emailverification.EmailVerification;

public interface DeleteEmailVerificationPort {

    void delete(EmailVerification emailVerification);

}
