package net.akive.api.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.domain.Email;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;
import static net.akive.api.user.domain.LoginType.EMAIL;

@Getter
@Builder(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class User {

    private Long userId;

    private Email email;

    private UserRole role;

    private LoginType loginType;

    private String passwordHash;

    private UserDetail userDetail;

    private Withdrawal withdrawal;

    private Instant createdAt;

    public static User reconstruct(
            Long userId,
            Email email,
            UserRole role,
            LoginType loginType,
            String passwordHash,
            UserDetail userDetail,
            Withdrawal withdrawal,
            Instant createdAt
    ) {
        return User.builder()
                .userId(userId)
                .email(requireNonNull(email))
                .role(requireNonNull(role))
                .loginType(requireNonNull(loginType))
                .passwordHash(validatePasswordHash(passwordHash))
                .userDetail(requireNonNull(userDetail))
                .withdrawal(requireNonNull(withdrawal))
                .createdAt(requireNonNull(createdAt))
                .build();
    }

    public static User signUpByEmail(Email email, UserRole role, String passwordHash, String nickname) {
        return User.builder()
                .email(requireNonNull(email))
                .role(requireNonNull(role))
                .loginType(EMAIL)
                .passwordHash(validateRequiredPasswordHash(passwordHash))
                .userDetail(UserDetail.initial(nickname))
                .withdrawal(Withdrawal.none())
                .build();
    }

    public boolean hasPassword() {
        return this.passwordHash != null;
    }

    public void updatePasswordHash(String targetPasswordHash) {
        this.passwordHash = validateRequiredPasswordHash(targetPasswordHash);
    }

    public void updateUserDetail(String targetNickname, UUID targetProfileImageAssetId, String targetAim) {
        this.userDetail = new UserDetail(targetNickname, targetProfileImageAssetId, targetAim);
    }

    public void withdraw(Instant withdrawnAt, String reason) {
        this.withdrawal = Withdrawal.completedWith(withdrawnAt, reason);
    }

    public boolean isWithdrawn() {
        return this.withdrawal.isWithdrawn();
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
