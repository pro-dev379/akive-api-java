package net.akive.api.core.config.aws;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class CdnPropertiesTest {

    @Test
    void CDN의_기본_URL과_객체_키로_객체_URL을_생성한다() {
        CdnProperties cdnProperties = new CdnProperties(URI.create("https://dev-cdn.akive.net/"));

        URI result = cdnProperties.resolve("/variants/profile-images/detail.webp");

        assertThat(result).isEqualTo(URI.create("https://dev-cdn.akive.net/variants/profile-images/detail.webp"));
    }
}
