package net.akive.api.auth.application.emailverification.provided;

import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationCodeVerifyCommand;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.common.exception.business.UnauthorizedException;

public interface EmailVerificationCodeVerifier {

    void verify(EmailVerificationCodeVerifyCommand command)
            throws BadRequestException, NotFoundException, UnauthorizedException, ConflictException;
}
