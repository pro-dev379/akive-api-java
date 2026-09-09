package net.akive.api.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.domain.Email;

import java.time.LocalDateTime;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;
import static net.akive.api.user.domain.LoginType.EMAIL;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class User {

    private Long userId;

    private Email email;

    private RoleType roleType;

    private LoginType loginType;

    private String passwordHash;

    private UserDetail userDetail;

    private Withdrawal withdrawal;

    public static User reconstruct(
            Long userId,
            Email email,
            RoleType roleType,
            LoginType loginType,
            String passwordHash,
            UserDetail userDetail,
            Withdrawal withdrawal
    ) {
        return User.builder()
                .userId(userId)
                .email(requireNonNull(email))
                .roleType(requireNonNull(roleType))
                .loginType(requireNonNull(loginType))
                .passwordHash(validatePasswordHash(passwordHash))
                .userDetail(requireNonNull(userDetail))
                .withdrawal(requireNonNull(withdrawal))
                .build();
    }

    public static User signUpByEmail(Email email, RoleType roleType, String passwordHash, String nickname) {
        return User.builder()
                .email(requireNonNull(email))
                .roleType(requireNonNull(roleType))
                .loginType(EMAIL)
                .passwordHash(validateRequiredPasswordHash(passwordHash))
                .userDetail(UserDetail.initial(nickname))
                .withdrawal(Withdrawal.none())
                .build();
    }

    public boolean hasPassword() {
        return this.passwordHash != null;
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = validateRequiredPasswordHash(passwordHash);
    }

    public void updateUserDetail(String nickname, String imgUrl, String aim) {
        this.userDetail = UserDetail.of(nickname, imgUrl, aim);
    }

    public void withdraw(LocalDateTime withdrawnAt, String reason) {
        this.withdrawal = Withdrawal.completedWith(withdrawnAt, reason);
    }

    private static String validatePasswordHash(String passwordHash) {
        return passwordHash != null ? requireNotBlank(passwordHash) : null;
    }

    private static String validateRequiredPasswordHash(String passwordHash) {
        return requireNotBlank(passwordHash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User that = (User) o;

        return this.userId != null && Objects.equals(this.userId, that.userId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
