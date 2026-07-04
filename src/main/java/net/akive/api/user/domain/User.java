package net.akive.api.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.user.domain.LoginType.EMAIL;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class User {

    private Long userId;
    private String email;
    private RoleType roleType;
    private LoginType loginType;
    private String nickname;
    private String password;
    private String imgUrl;
    private String aim;
    private boolean leave;
    private String leaveReason;

    public static User create(
            Long userId,
            String email,
            RoleType roleType,
            LoginType loginType,
            String nickname,
            String password,
            String imgUrl,
            String aim,
            boolean leave,
            String leaveReason
    ) {
        return User.builder()
                .userId(userId)
                .email(email)
                .roleType(roleType)
                .loginType(loginType)
                .nickname(nickname)
                .password(password)
                .imgUrl(imgUrl)
                .aim(aim)
                .leave(leave)
                .leaveReason(leaveReason)
                .build();
    }

    public static User signUpByEmail(
            String email,
            String nickname,
            String encodedPassword,
            RoleType roleType
    ) {
        return User.builder()
                .email(email)
                .roleType(roleType)
                .loginType(EMAIL)
                .nickname(nickname)
                .password(encodedPassword)
                .leave(false)
                .build();
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
