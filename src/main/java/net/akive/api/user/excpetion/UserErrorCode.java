package net.akive.api.user.excpetion;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_1", "User not found."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_409_1", "Email already in use.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
