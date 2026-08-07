package net.akive.api.user.adapter.generation;

import net.akive.api.user.application.required.GenerateNicknamePort;
import net.akive.api.user.domain.RoleType;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static net.akive.api.user.domain.UserPolicy.RANDOM_NICKNAME_SUFFIX_LENGTH;

@Component
class RandomNicknameGenerator implements GenerateNicknamePort {

    @Override
    public String generate(RoleType roleType) {
        return roleType.name().toLowerCase()
                + "@"
                + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, RANDOM_NICKNAME_SUFFIX_LENGTH);
    }
}
