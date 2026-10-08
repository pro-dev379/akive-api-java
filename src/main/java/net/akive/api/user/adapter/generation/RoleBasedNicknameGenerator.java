package net.akive.api.user.adapter.generation;

import net.akive.api.user.application.required.NicknameGenerator;
import net.akive.api.user.domain.UserRole;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static net.akive.api.user.domain.UserPolicy.ROLE_BASED_NICKNAME_SUFFIX_LENGTH;

@Component
class RoleBasedNicknameGenerator implements NicknameGenerator {

    @Override
    public String generate(UserRole role) {
        return role.name().toLowerCase()
                + "@"
                + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, ROLE_BASED_NICKNAME_SUFFIX_LENGTH);
    }
}
