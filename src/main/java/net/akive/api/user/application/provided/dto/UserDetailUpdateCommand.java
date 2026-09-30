package net.akive.api.user.application.provided.dto;

import java.util.UUID;

import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.user.domain.UserPolicy.NICKNAME_MAX_LENGTH;

public record UserDetailUpdateCommand(
        Long userId,
        String targetNickname,
        String targetAim,
        UUID targetProfileImageAssetId
) {

    public UserDetailUpdateCommand {
        requirePositive(userId);
        targetNickname = requireMaxLength(requireNotBlank(targetNickname), NICKNAME_MAX_LENGTH);
    }
}
