package net.akive.api.user.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_INPUT_VALUE;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.REQUIRED_VALUE_MISSING;
import static net.akive.api.user.domain.fixture.WithdrawalFixture.WITHDRAWAL_REASON;
import static net.akive.api.user.domain.fixture.WithdrawalFixture.WITHDRAWN_AT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class WithdrawalTest {

    @Test
    void 탈퇴하지_않은_상태를_생성한다() {
        Withdrawal withdrawal = Withdrawal.none();

        assertThat(withdrawal.withdrawnAt()).isNull();
        assertThat(withdrawal.withdrawalReason()).isNull();
    }

    @Test
    void 탈퇴_상태를_생성한다() {
        Withdrawal withdrawal = Withdrawal.completedWith(WITHDRAWN_AT, WITHDRAWAL_REASON);

        assertThat(withdrawal.withdrawnAt()).isEqualTo(WITHDRAWN_AT);
        assertThat(withdrawal.withdrawalReason()).isEqualTo(WITHDRAWAL_REASON);
    }

    @Test
    void 탈퇴_상태에서_탈퇴_사유가_없으면_생성에_실패한다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> Withdrawal.completedWith(WITHDRAWN_AT, null))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 탈퇴하지_않은_상태에서_탈퇴_사유가_있으면_생성에_실패한다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> new Withdrawal(null, WITHDRAWAL_REASON))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_INPUT_VALUE);
    }

    @Test
    void 탈퇴_여부를_반환한다() {
        assertThat(Withdrawal.none().isWithdrawn()).isFalse();
        assertThat(Withdrawal.completedWith(WITHDRAWN_AT, WITHDRAWAL_REASON).isWithdrawn()).isTrue();
    }
}
