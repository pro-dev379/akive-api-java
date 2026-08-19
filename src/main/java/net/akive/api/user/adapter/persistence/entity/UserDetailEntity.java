package net.akive.api.user.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.user.domain.UserDetail;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Embeddable
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class UserDetailEntity {

    @Column(nullable = false)
    private String nickname;

    private String imgUrl;

    private String aim;

    public static UserDetailEntity from(UserDetail userDetail) {
        return new UserDetailEntity(userDetail.nickname(), userDetail.imgUrl(), userDetail.aim());
    }

    public UserDetail to() {
        return new UserDetail(this.nickname, this.imgUrl, this.aim);
    }
}
