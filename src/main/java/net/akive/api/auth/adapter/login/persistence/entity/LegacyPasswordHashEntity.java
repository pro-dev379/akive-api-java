package net.akive.api.auth.adapter.login.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.auth.domain.login.LegacyPasswordHash;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Embeddable
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class LegacyPasswordHashEntity {

    @Column(name = "password_hash", nullable = false)
    private String hash;

    @Column(name = "password_salt", nullable = false)
    private String salt;

    public static LegacyPasswordHashEntity from(LegacyPasswordHash passwordHash) {
        return new LegacyPasswordHashEntity(passwordHash.hash(), passwordHash.salt());
    }

    public LegacyPasswordHash to() {
        return new LegacyPasswordHash(this.hash, this.salt);
    }
}
