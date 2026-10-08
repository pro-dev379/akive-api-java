package net.akive.api.user.adapter.webapi.dto.response;

import net.akive.api.image.domain.ProcessingStatus;
import net.akive.api.user.domain.User;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

public record UserFindResponse(
        String email,
        String role,
        String loginType,
        UserDetailResponse userDetail,
        Instant createdAt
) {

    public static UserFindResponse of(User user, ProfileImageResponse profileImage) {
        return new UserFindResponse(
                user.getEmail().value(),
                user.getRole().name(),
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

    public record ProfileImageResponse(UUID assetId, ProcessingStatus processingStatus, URI url) {

        public static ProfileImageResponse of(UUID assetId, ProcessingStatus processingStatus, URI url) {
            return new ProfileImageResponse(assetId, processingStatus, url);
        }
    }
}
