package net.akive.api.user.application.required;

import net.akive.api.user.domain.UserRole;

public interface NicknameGenerator {

    String generate(UserRole role);
}
