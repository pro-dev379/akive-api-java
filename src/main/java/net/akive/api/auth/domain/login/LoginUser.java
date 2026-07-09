package net.akive.api.auth.domain.login;

import net.akive.api.common.domain.Email;
import net.akive.api.user.domain.RoleType;

import static net.akive.api.common.domain.DomainValidator.*;

public record LoginUser(
        Long userId,
        Email email,
        String password,
        RoleType roleType
) {

    public LoginUser {
        requirePositive(userId);
        requireNonNull(email);
        if (password != null) {
            password = requireNotBlank(password);
        }
        requireNonNull(roleType);
    }

    public static LoginUser create(Long userId, Email email, String password, RoleType roleType) {
        return new LoginUser(
                userId,
                email,
                password,
                roleType
        );
    }

    public boolean hasPassword() {
        return password != null;
    }

    public String passwordValue() {
        return password;
    }
}
