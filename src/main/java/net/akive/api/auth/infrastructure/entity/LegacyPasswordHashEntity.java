package net.akive.api.auth.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public static LegacyPasswordHashEntity of(String hash, String salt) {
        return new LegacyPasswordHashEntity(hash, salt);
    }
}
