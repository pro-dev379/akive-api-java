package net.akive.api.user.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.domain.Email;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.user.domain.LoginType;
import net.akive.api.user.domain.User;
import net.akive.api.user.domain.RoleType;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

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
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'USER'")
    private RoleType roleType;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'EMAIL'")
    private LoginType loginType;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String password;

    private String imgUrl;
    private String aim;

    @Column(name="is_leave", columnDefinition = "TINYINT(1) UNSIGNED DEFAULT 0", nullable = false)
    private boolean leave;

    private String leaveReason;

    public static UserEntity reference(Long userId) {
        UserEntity userEntity = new UserEntity();
        userEntity.userId = userId;

        return userEntity;
    }

    public static UserEntity from(User user) {
        return new UserEntity(
                user.getUserId(),
                user.getEmail().value(),
                user.getRoleType(),
                user.getLoginType(),
                user.getNickname(),
                user.getPassword(),
                user.getImgUrl(),
                user.getAim(),
                user.isLeave(),
                user.getLeaveReason()
        );
    }

    public User to() {
        return User.create(
                this.userId,
                new Email(this.email),
                this.roleType,
                this.loginType,
                this.nickname,
                this.password,
                this.imgUrl,
                this.aim,
                this.leave,
                this.leaveReason
        );
    }
}
