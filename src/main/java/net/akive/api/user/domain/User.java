package net.akive.api.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.domain.Email;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.user.domain.LoginType.EMAIL;
import static net.akive.api.user.domain.UserPolicy.AIM_MAX_LENGTH;
import static net.akive.api.user.domain.UserPolicy.NICKNAME_MAX_LENGTH;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class User {

    private Long userId;
    private Email email;
    private RoleType roleType;
    private LoginType loginType;
    private String nickname;
    private String password;
    private String imgUrl;
    private String aim;
    private boolean leave;
    private String leaveReason;

    public static User reconstruct(
            Long userId,
            Email email,
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
                .email(requireNonNull(email))
                .roleType(requireNonNull(roleType))
                .loginType(requireNonNull(loginType))
                .nickname(validateNickname(nickname))
                .password(validateEncodedPassword(password))
                .imgUrl(imgUrl)
                .aim(requireMaxLengthIfPresent(aim, AIM_MAX_LENGTH))
                .leave(leave)
                .leaveReason(leaveReason)
                .build();
    }

    public static User signUpByEmail(
            Email email,
            String nickname,
            String encodedPassword,
            RoleType roleType
    ) {
        return User.builder()
                .email(requireNonNull(email))
                .roleType(requireNonNull(roleType))
                .loginType(requireNonNull(EMAIL))
                .nickname(validateNickname(nickname))
                .password(validateEncodedPassword(encodedPassword))
                .leave(false)
                .build();
    }

    public void updatePassword(String encodedPassword) {
        this.password = validateEncodedPassword(encodedPassword);
    }

    private static String validateNickname(String nickname) {
        return requireMaxLength(requireNotBlank(nickname), NICKNAME_MAX_LENGTH);
    }

    private static String validateEncodedPassword(String encodedPassword) {
        return requireNotBlank(encodedPassword);
    }
}
