package net.akive.api.user.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.user.domain.UserDetail;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static org.hibernate.type.SqlTypes.BINARY;

@Getter
@Embeddable
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class UserDetailEntity {

    @Column(nullable = false)
    private String nickname;

    @JdbcTypeCode(BINARY)
    @Column(name = "image_asset_id", columnDefinition = "BINARY(16)")
    private UUID profileImageAssetId;

    private String aim;

    public static UserDetailEntity from(UserDetail userDetail) {
        return new UserDetailEntity(
                userDetail.nickname(),
                userDetail.profileImageAssetId(),
                userDetail.aim()
        );
    }

    public UserDetail to() {
        return new UserDetail(this.nickname, this.profileImageAssetId, this.aim);
    }
}
