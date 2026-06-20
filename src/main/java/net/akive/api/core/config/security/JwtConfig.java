package net.akive.api.core.config.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    private static final String HMAC_SHA_256 = "HmacSHA256";

    private final JwtProperties jwtProperties;

    @Bean
    public JwtEncoder accessTokenJwtEncoder() {
        return createEncoder(jwtProperties.accessToken().secret());
    }

    @Bean
    public JwtEncoder refreshTokenJwtEncoder() {
        return createEncoder(jwtProperties.refreshToken().secret());
    }

    @Bean
    @Primary
    public JwtDecoder accessTokenJwtDecoder() {
        return createDecoder(jwtProperties.accessToken().secret());
    }

    @Bean
    public JwtDecoder refreshTokenJwtDecoder() {
        return createDecoder(jwtProperties.refreshToken().secret());
    }

    private JwtEncoder createEncoder(String secret) {
        SecretKey secretKey = createSecretKey(secret);

        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    private JwtDecoder createDecoder(String secret) {
        SecretKey secretKey = createSecretKey(secret);

        return NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(HS256)
                .build();
    }

    private SecretKey createSecretKey(String secret) {
        return new SecretKeySpec(secret.getBytes(UTF_8), HMAC_SHA_256);
    }
}
