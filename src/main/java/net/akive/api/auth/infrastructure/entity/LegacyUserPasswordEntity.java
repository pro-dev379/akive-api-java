package net.akive.api.auth.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.auth.domain.login.LegacyUserPassword;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.user.infrastructure.entity.UserEntity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "legacy_user_password")
@Getter
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class LegacyUserPasswordEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long legacyUserPasswordId;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Embedded
    private LegacyPasswordHash legacyPasswordHash;

    public static LegacyUserPasswordEntity fromModel(LegacyUserPassword legacyUserPassword) {
        LegacyUserPasswordEntity legacyUserPasswordEntity = new LegacyUserPasswordEntity();
        legacyUserPasswordEntity.user = UserEntity.reference(legacyUserPassword.userId());
        legacyUserPasswordEntity.legacyPasswordHash = LegacyPasswordHash.of(legacyUserPassword.hash(), legacyUserPassword.salt());

        return legacyUserPasswordEntity;
    }

    public LegacyUserPassword toModel() {
        return new LegacyUserPassword(
                user.getUserId(),
                legacyPasswordHash.getHash(),
                legacyPasswordHash.getSalt()
        );
    }
}
