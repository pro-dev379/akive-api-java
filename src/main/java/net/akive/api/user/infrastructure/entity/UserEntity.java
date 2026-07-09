package net.akive.api.user.infrastructure.entity;

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

    @Column(nullable = false, length = 200)
    private String email;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'USER'")
    private RoleType roleType;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'EMAIL'")
    private LoginType loginType;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(length = 200)
    private String imgUrl;

    @Column(length = 200)
    private String aim;

    @Column(name="is_leave", columnDefinition = "TINYINT(1) UNSIGNED DEFAULT 0", nullable = false)
    private boolean leave;

    @Column(length = 300)
    private String leaveReason;

    public static UserEntity reference(Long userId) {
        UserEntity userEntity = new UserEntity();
        userEntity.userId = userId;

        return userEntity;
    }

    public static UserEntity fromModel(User user) {
        UserEntity userEntity = new UserEntity();
        userEntity.userId = user.getUserId();
        userEntity.email = user.getEmail().value();
        userEntity.roleType = user.getRoleType();
        userEntity.loginType = user.getLoginType();
        userEntity.nickname = user.getNickname();
        userEntity.password = user.getPassword();
        userEntity.imgUrl = user.getImgUrl();
        userEntity.aim = user.getAim();
        userEntity.leave = user.isLeave();
        userEntity.leaveReason = user.getLeaveReason();

        return userEntity;
    }

    public User toModel() {
        return User.create(
                this.userId,
                Email.of(this.email),
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
