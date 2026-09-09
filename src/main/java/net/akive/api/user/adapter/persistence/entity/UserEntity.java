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
import static net.akive.api.user.domain.LoginType.EMAIL;
import static net.akive.api.user.domain.RoleType.MEMBER;

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
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'MEMBER'")
    private RoleType roleType = MEMBER;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'EMAIL'")
    private LoginType loginType = EMAIL;

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
