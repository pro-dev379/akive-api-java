package net.akive.api.user.domain.fixture;

import net.akive.api.user.domain.UserDetail;

import java.util.UUID;

public class UserDetailFixture {

    private UserDetailFixture() {
    }

    public static final String NICKNAME = "member@abcdefg1234";
    public static final UUID PROFILE_IMAGE_ASSET_ID = UUID.fromString("0199a8e2-7c31-7abc-8123-456789abcdef");
    public static final UUID ANOTHER_PROFILE_IMAGE_ASSET_ID = UUID.fromString("0199a8e2-7c31-7abc-8123-456789abcdee");
    public static final String AIM = "목표";

    public static UserDetail createUserDetail() {
        return createUserDetail(PROFILE_IMAGE_ASSET_ID);
    }

    public static UserDetail createUserDetail(UUID profileImageAssetId) {
        return new UserDetail(NICKNAME, profileImageAssetId, AIM);
    }
}
