package net.akive.api.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.user.domain.LoginType.EMAIL;

@Getter
@Builder
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
}
