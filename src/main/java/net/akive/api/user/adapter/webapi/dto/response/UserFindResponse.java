package net.akive.api.user.adapter.webapi.dto.response;

import net.akive.api.user.domain.User;

import java.time.LocalDateTime;

public record UserFindResponse(
        String email,
        String roleType,
        String loginType,
        UserDetailResponse userDetail,
        LocalDateTime createdAt
) {

    public static UserFindResponse from(User user) {
        return new UserFindResponse(
                user.getEmail().value(),
                user.getRoleType().name(),
                user.getLoginType().name(),
                new UserDetailResponse(
                        user.getUserDetail().nickname(),
                        user.getUserDetail().aim(),
                        user.getUserDetail().imgUrl()
                ),
                user.getCreatedAt()
        );
    }

    public record UserDetailResponse(String nickname, String aim, String imgUrl) {}
}
