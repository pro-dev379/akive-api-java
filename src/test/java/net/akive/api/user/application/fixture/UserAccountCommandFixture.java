package net.akive.api.user.application.fixture;

import net.akive.api.user.application.provided.dto.UserSignUpByEmailCommand;
import net.akive.api.user.application.provided.dto.UserWithdrawCommand;

import static net.akive.api.user.domain.fixture.UserDetailFixture.NICKNAME;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static net.akive.api.user.domain.fixture.WithdrawalFixture.WITHDRAWAL_REASON;

public class UserAccountCommandFixture {

    public static UserSignUpByEmailCommand createUserSignUpByEmailCommand() {
        return new UserSignUpByEmailCommand(EMAIL, PASSWORD, NICKNAME);
    }

    public static UserWithdrawCommand createUserWithdrawCommand() {
        return createUserWithdrawCommand(USER_ID);
    }

    public static UserWithdrawCommand createUserWithdrawCommand(Long userId) {
        return new UserWithdrawCommand(userId, WITHDRAWAL_REASON);
    }
}
