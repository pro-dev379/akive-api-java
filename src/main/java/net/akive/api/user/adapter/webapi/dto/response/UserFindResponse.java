package net.akive.api.user.adapter.webapi.dto.response;

import net.akive.api.user.domain.User;

public record UserFindResponse(
        String email,
        String nickname,
        String roleType,
        String loginType,
        String aim,
        String imgUrl
) {

    public static UserFindResponse from(User user) {
        return new UserFindResponse(
                user.getEmail().value(),
                user.getUserDetail().nickname(),
                user.getRoleType().name(),
                user.getLoginType().name(),
                user.getUserDetail().aim(),
                user.getUserDetail().imgUrl()
        );
    }
}
