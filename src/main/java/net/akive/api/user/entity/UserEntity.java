package net.akive.api.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.user.domain.LoginType;
import net.akive.api.user.domain.UserType;

@Entity
@Table(
    name = "user",
    uniqueConstraints = {
        @UniqueConstraint(name = "ux-user-email", columnNames = "email"),
        @UniqueConstraint(name = "ux-user-nickname", columnNames = "nickname")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String email;

    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'USER'")
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'EMAIL'")
    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(length = 500)
    private String pw;

    @Column(length = 500)
    private String salt;

    @Column(length = 200)
    private String imgUrl;

    @Column(length = 200)
    private String aim;

    @Column(name="is_leave", columnDefinition = "TINYINT(1) UNSIGNED DEFAULT 0", nullable = false)
    private boolean leave;

    @Column(length = 300)
    private String leaveReason;

    public void leave(String reason) {
        this.leave = true;
        this.leaveReason = reason;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeProfileImage(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
