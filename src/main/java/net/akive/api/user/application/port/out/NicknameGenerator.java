package net.akive.api.user.application.port.out;

import net.akive.api.user.domain.RoleType;

public interface NicknameGenerator {

    String generate(RoleType roleType);
}
