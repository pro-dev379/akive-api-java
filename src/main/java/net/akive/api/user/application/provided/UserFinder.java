package net.akive.api.user.application.provided;

import net.akive.api.user.domain.User;

public interface UserFinder {

    User find(Long userId);
}
