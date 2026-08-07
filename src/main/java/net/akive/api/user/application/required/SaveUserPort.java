package net.akive.api.user.application.required;

import net.akive.api.user.domain.User;

public interface SaveUserPort {

    User save(User user);
}
