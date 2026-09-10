package net.akive.api.user.domain.fixture;

import net.akive.api.user.domain.UserDetail;

public class UserDetailFixture {

    public static final String NICKNAME = "member@abcdefg1234";
    public static final String IMG_URL = "https://example.com/image.png";
    public static final String AIM = "목표";

    public static UserDetail createUserDetail() {
        return new UserDetail(NICKNAME, IMG_URL, AIM);
    }
}
