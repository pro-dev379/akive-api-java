package net.akive.api.user.application.port.out;

import net.akive.api.common.domain.Email;

public interface ValidateUserPort {

    boolean existsByUserIdAndLeaveFalse(Long userId);
    boolean existsByEmailAndLeaveFalse(Email email);
    boolean existsByEmail(Email email);
}
