package net.akive.api.core.config.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "email")
public record EmailProperties(FromProperties from) {

    public record FromProperties(String verification, String welcome, String support) {
    }
}
