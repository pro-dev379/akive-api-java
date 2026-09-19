package net.akive.api.user.domain.fixture;

import net.akive.api.common.domain.Email;
import net.akive.api.user.domain.User;
import net.akive.api.user.domain.Withdrawal;

import java.time.LocalDateTime;

import static net.akive.api.common.domain.EmailFixture.createEmail;
import static net.akive.api.user.domain.fixture.UserDetailFixture.NICKNAME;
import static net.akive.api.user.domain.fixture.UserFixture.EMAIL;
import static net.akive.api.user.domain.fixture.UserFixture.createUser;

public class WithdrawalFixture {

    private WithdrawalFixture() {
    }

    public static final Email WITHDRAWN_USER_EMAIL = createEmail("withdrawn-user@test.com");
    public static final String WITHDRAWAL_REASON = "탈퇴 사유";
    public static final LocalDateTime WITHDRAWN_AT = LocalDateTime.of(2026, 9, 9, 12, 0);

    public static User createWithdrawnUser() {
        return createUser(EMAIL, NICKNAME, Withdrawal.completedWith(WITHDRAWN_AT, WITHDRAWAL_REASON));
    }

    public static User createWithdrawnUser(Email email) {
        return createUser(email, NICKNAME, Withdrawal.completedWith(WITHDRAWN_AT, WITHDRAWAL_REASON));
    }
}
