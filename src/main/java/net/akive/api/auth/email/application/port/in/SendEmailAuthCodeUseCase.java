package net.akive.api.auth.email.application.port.in;

import net.akive.api.auth.email.application.dto.EmailAuthCodeSendResult;
import net.akive.api.auth.email.domain.EmailAuthPurpose;

public interface SendEmailAuthCodeUseCase {

    EmailAuthCodeSendResult sendAuthCode(String email, EmailAuthPurpose authPurpose);
}
