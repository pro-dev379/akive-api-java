package net.akive.api.core.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

import static net.akive.api.core.config.security.CookieRequestProtectionFilter.CSRF_PROTECTION_HEADER_NAME;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(CorsProperties.class)
public class SecurityConfig {

    private final JwtDecoder accessTokenJwtDecoder;
    private final TokenAuthenticationEntryPoint tokenAuthenticationEntryPoint;
    private final CorsProperties corsProperties;
    private final ObjectMapper objectMapper;

    @Bean
    @Order(1)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .securityMatcher(
                        "/auth/tokens",
                        "/auth/tokens/**",
                        "/auth/login/**",
                        "/auth/email-verifications/**",
                        "/auth/password-reset",
                        "/members/email",
                        "/members/check-nickname",
                        "/legal-documents/*",
                        "/legal-documents/types/**",
                        "/legal-documents/action-contexts/*/current/headers",
                        "/actuator/health",
                        "/actuator/health/**"
                )
                .cors(Customizer.withDefaults())
                .addFilterAfter(new CookieRequestProtectionFilter(objectMapper), CorsFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2
                                .jwt(jwt -> jwt.decoder(accessTokenJwtDecoder))
                                .authenticationEntryPoint(tokenAuthenticationEntryPoint)
                )
                .build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(corsProperties.allowedOrigins());
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));
        corsConfiguration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Client-Instance-Id",
                CSRF_PROTECTION_HEADER_NAME
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
