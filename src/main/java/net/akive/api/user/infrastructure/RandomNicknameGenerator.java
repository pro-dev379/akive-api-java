package net.akive.api.user.infrastructure;

import net.akive.api.user.application.port.out.GenerateNicknamePort;
import net.akive.api.user.domain.RoleType;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class RandomNicknameGenerator implements GenerateNicknamePort {

    @Override
    public String generate(RoleType roleType) {
        return roleType.name().toLowerCase()
                + "@"
                + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8);
    }
}
