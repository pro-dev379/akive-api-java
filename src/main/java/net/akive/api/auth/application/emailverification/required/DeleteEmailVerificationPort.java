package net.akive.api.auth.application.emailverification.required;

import net.akive.api.auth.domain.emailverification.EmailVerification;

public interface DeleteEmailVerificationPort {

    void delete(EmailVerification emailVerification);
}
