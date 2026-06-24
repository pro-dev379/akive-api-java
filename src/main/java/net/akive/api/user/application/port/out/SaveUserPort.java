package net.akive.api.user.application.port.out;

import net.akive.api.user.domain.User;

public interface SaveUserPort {

    User save(User user);
}
