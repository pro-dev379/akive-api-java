package net.akive.api.auth.application.login.service;

import net.akive.api.auth.domain.login.LegacyPasswordHash;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
class LegacyPasswordVerifier {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
    private static final int ITERATIONS = 104_906;
    private static final int KEY_LENGTH_BITS = 64 * 8;

    boolean matches(String rawPassword, LegacyPasswordHash passwordHash) {
        String hashedPassword = hash(rawPassword, passwordHash.salt());

        return MessageDigest.isEqual(hashedPassword.getBytes(UTF_8), passwordHash.hash().getBytes(UTF_8));
    }

    private String hash(String rawPassword, String salt) {
        try {
            PBEKeySpec pbeKeySpec = new PBEKeySpec(
                    rawPassword.toCharArray(),
                    salt.getBytes(UTF_8),
                    ITERATIONS,
                    KEY_LENGTH_BITS

            );

            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] secretKey = keyFactory.generateSecret(pbeKeySpec).getEncoded();

            return Base64.getEncoder().encodeToString(secretKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Failed to generate legacy password hash", e);
        }
    }
}
