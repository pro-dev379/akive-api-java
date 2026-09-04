package net.akive.api.user.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.user.domain.UserPolicy.LEAVE_REASON_MAX_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LeaveTest {

    private static final String REASON = "탈퇴 사유";

    @Test
    void 탈퇴하지_않은_상태를_생성한다() {
        Leave leave = Leave.none();

        assertThat(leave.isLeave()).isFalse();
        assertThat(leave.reason()).isNull();
    }

    @Test
    void 탈퇴_상태를_생성한다() {
        Leave leave = Leave.completed(REASON);

        assertThat(leave.isLeave()).isTrue();
        assertThat(leave.reason()).isEqualTo(REASON);
    }

    @Test
    void 탈퇴_사유의_앞뒤에_공백이_있으면_제거된다() {
        Leave leave = Leave.completed("  탈퇴 사유  ");

        assertThat(leave.reason()).isEqualTo("탈퇴 사유");
    }

    @Test
    void 탈퇴_사유는_최대_글자_수일_수_있다() {
        String reason = "a".repeat(LEAVE_REASON_MAX_LENGTH);

        Leave leave = Leave.completed(reason);

        assertThat(leave.reason()).isEqualTo(reason);
    }

    @Test
    void 탈퇴_상태에서_탈퇴_사유가_없으면_생성에_실패한다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> Leave.completed(null))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> Leave.completed("   "))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 탈퇴_사유가_최대_글자_수를_초과하면_생성에_실패한다() {
        String reason = "a".repeat(LEAVE_REASON_MAX_LENGTH + 1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> Leave.completed(reason))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }

    @Test
    void 탈퇴하지_않은_상태에서_탈퇴_사유가_있으면_생성에_실패한다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> new Leave(false, REASON))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_INPUT_VALUE);
    }
}
