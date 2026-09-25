package net.akive.api.file.domain.fixture;

import java.net.URI;
import java.time.Instant;

import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public class FileFixture {

    public static final String FILE_KEY = "profile-images/" + USER_ID + "/image.jpg";
    public static final String CONTENT_TYPE = "image/jpeg";
    public static final URI UPLOAD_URL = URI.create("https://akive-user.s3.amazonaws.com/upload");
    public static final Instant EXPIRES_AT = Instant.parse("2026-09-25T00:10:00Z");

    private FileFixture() {
    }
}
