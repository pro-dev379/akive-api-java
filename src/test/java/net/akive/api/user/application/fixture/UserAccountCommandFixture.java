package net.akive.api.user.application.fixture;

import net.akive.api.user.application.provided.dto.UserDetailUpdateCommand;
import net.akive.api.user.application.provided.dto.UserSignUpByEmailCommand;
import net.akive.api.user.application.provided.dto.UserWithdrawCommand;

import static net.akive.api.user.domain.fixture.UserDetailFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static net.akive.api.user.domain.fixture.WithdrawalFixture.WITHDRAWAL_REASON;

public class UserAccountCommandFixture {

    private UserAccountCommandFixture() {
    }

    public static UserSignUpByEmailCommand createUserSignUpByEmailCommand() {
        return new UserSignUpByEmailCommand(EMAIL, PASSWORD, NICKNAME);
    }

    public static UserDetailUpdateCommand createUserDetailUpdateCommand() {
        return createUserDetailUpdateCommand(USER_ID);
    }

    public static UserDetailUpdateCommand createUserDetailUpdateCommand(Long userId) {
        return new UserDetailUpdateCommand(userId, NICKNAME, PROFILE_IMAGE_ASSET_ID, AIM);
    }

    public static UserWithdrawCommand createUserWithdrawCommand() {
        return createUserWithdrawCommand(USER_ID);
    }

    public static UserWithdrawCommand createUserWithdrawCommand(Long userId) {
        return new UserWithdrawCommand(userId, WITHDRAWAL_REASON);
    }
}
