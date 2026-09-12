package net.akive.api.user.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.domain.Email;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.user.domain.LoginType;
import net.akive.api.user.domain.RoleType;
import net.akive.api.user.domain.User;
import net.akive.api.user.domain.Withdrawal;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(
    name = "`user`",
    uniqueConstraints = {
        @UniqueConstraint(name = "ux-user-email", columnNames = "email"),
        @UniqueConstraint(name = "ux-user-nickname", columnNames = "nickname")
    }
)
@Getter
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class UserEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String email;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private RoleType roleType;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private LoginType loginType;

    private String passwordHash;

    @Embedded
    private UserDetailEntity userDetail;

    @Embedded
    private WithdrawalEntity withdrawal;

    public static UserEntity from(User user) {
        return new UserEntity(
                user.getUserId(),
                user.getEmail().value(),
                user.getRoleType(),
                user.getLoginType(),
                user.getPasswordHash(),
                UserDetailEntity.from(user.getUserDetail()),
                WithdrawalEntity.from(user.getWithdrawal())
        );
    }

    public User to() {
        return User.reconstruct(
                this.userId,
                new Email(this.email),
                this.roleType,
                this.loginType,
                this.passwordHash,
                this.userDetail.to(),
                this.withdrawal == null ? Withdrawal.none() : this.withdrawal.to()
        );
    }
}
