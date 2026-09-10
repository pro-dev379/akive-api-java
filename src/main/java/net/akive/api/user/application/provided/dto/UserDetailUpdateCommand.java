package net.akive.api.user.application.provided.dto;

import static net.akive.api.common.domain.DomainValidator.requireNotBlank;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record UserDetailUpdateCommand(
        Long userId,
        String nickname,
        String imgUrl,
        String aim
) {

    public UserDetailUpdateCommand {
        requirePositive(userId);
        requireNotBlank(nickname);
    }
}
