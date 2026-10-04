package net.akive.api.image.adapter.storage.fixture;

import java.net.URI;
import java.time.Instant;

import static net.akive.api.image.domain.fixture.ImageAssetFixture.IMAGE_ASSET_ID;

public class S3ImageStorageFixture {

    public static final String BUCKET_NAME = "akive-bucket-dev";
    public static final String ORIGINAL_IMAGE_OBJECT_PREFIX =
            "uploads/profile-images/" + IMAGE_ASSET_ID + "/original.";
    public static final String ORIGINAL_IMAGE_OBJECT_KEY = ORIGINAL_IMAGE_OBJECT_PREFIX + "jpg";
    public static final String CONTENT_TYPE = "image/jpeg";
    public static final URI UPLOAD_URL = URI.create(
            "https://" + BUCKET_NAME + ".s3.ap-northeast-2.amazonaws.com/" + ORIGINAL_IMAGE_OBJECT_KEY
                    + "?X-Amz-Signature=test-signature"
    );
    public static final Instant EXPIRES_AT = Instant.parse("2026-09-25T00:10:00Z");

    private S3ImageStorageFixture() {

    }
}
