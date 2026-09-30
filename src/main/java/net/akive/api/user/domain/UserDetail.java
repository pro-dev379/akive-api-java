package net.akive.api.user.domain;

import java.util.UUID;

import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.user.domain.UserPolicy.AIM_MAX_LENGTH;
import static net.akive.api.user.domain.UserPolicy.NICKNAME_MAX_LENGTH;

public record UserDetail(String nickname, UUID profileImageAssetId, String aim) {

    public UserDetail {
        nickname = requireMaxLength(requireNotBlank(nickname), NICKNAME_MAX_LENGTH);
        requireMaxLengthIfPresent(aim, AIM_MAX_LENGTH);
    }

    public static UserDetail initial(String nickname) {
        return new UserDetail(nickname, null, null);
    }
}
