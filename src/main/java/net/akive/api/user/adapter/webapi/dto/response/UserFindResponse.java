package net.akive.api.user.adapter.webapi.dto.response;

import net.akive.api.user.domain.User;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserFindResponse(
        String email,
        String roleType,
        String loginType,
        UserDetailResponse userDetail,
        LocalDateTime createdAt
) {

    public static UserFindResponse from(User user, ProfileImageResponse profileImage) {
        return new UserFindResponse(
                user.getEmail().value(),
                user.getRoleType().name(),
                user.getLoginType().name(),
                new UserDetailResponse(
                        user.getUserDetail().nickname(),
                        user.getUserDetail().aim(),
                        profileImage
                ),
                user.getCreatedAt()
        );
    }

    public record UserDetailResponse(String nickname, String aim, ProfileImageResponse profileImage) {}

    public record ProfileImageResponse(UUID assetId, URI url) {}
}
