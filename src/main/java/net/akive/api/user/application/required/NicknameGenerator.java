package net.akive.api.user.application.required;

import net.akive.api.user.domain.RoleType;

public interface NicknameGenerator {

    String generate(RoleType roleType);
}
