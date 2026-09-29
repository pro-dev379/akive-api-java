package net.akive.api.auth.adapter.login.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.auth.domain.login.LegacyUserPassword;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.internal.InvalidPersistedDataException;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.user.adapter.persistence.entity.UserEntity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(
        name = "legacy_user_password",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux-legacy_user_password-user_id", columnNames = "user_id")
        }
)
@Getter
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class LegacyUserPasswordEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long legacyUserPasswordId;

    @OneToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Embedded
    private LegacyPasswordHashEntity passwordHash;

    public static LegacyUserPasswordEntity from(LegacyUserPassword legacyUserPassword) {
        LegacyUserPasswordEntity legacyUserPasswordEntity = new LegacyUserPasswordEntity();

        legacyUserPasswordEntity.user = UserEntity.from(legacyUserPassword.user());
        legacyUserPasswordEntity.passwordHash =
                LegacyPasswordHashEntity.from(legacyUserPassword.passwordHash());

        return legacyUserPasswordEntity;
    }

    public LegacyUserPassword to() {
        try {
            return new LegacyUserPassword(this.user.to(), this.passwordHash.to());
        } catch (BusinessRuleViolationException e) {
            throw new InvalidPersistedDataException(
                    "Failed to reconstruct LegacyUserPassword: " + this.legacyUserPasswordId,
                    e
            );
        }
    }
}
