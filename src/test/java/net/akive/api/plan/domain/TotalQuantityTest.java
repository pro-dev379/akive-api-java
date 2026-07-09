package net.akive.api.plan.domain;

import net.akive.api.common.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.BLANK_VALUE;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.MAX_LENGTH_EXCEEDED;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.REQUIRED_VALUE_MISSING;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.VALUE_NOT_POSITIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TotalQuantityTest {

    private static final SessionalPlan VALID_SESSIONAL_PLAN = SessionalPlan.create(
            2L,
            1L,
            "세션",
            SessionPeriod.of(
                    LocalDate.of(2026, 7, 7),
                    LocalDate.of(2026, 7, 14)
            )
    );

    @Test
    void 세션_총량을_생성한다() {
        // when
        TotalQuantity totalQuantity = TotalQuantity.create(
                1L,
                VALID_SESSIONAL_PLAN,
                "수학",
                "과목",
                100,
                "문제",
                "메모"
        );

        // then
        assertThat(totalQuantity.getSessionalPlan()).isEqualTo(VALID_SESSIONAL_PLAN);
        assertThat(totalQuantity.getSubject()).isEqualTo("수학");
        assertThat(totalQuantity.getSubjectCategory()).isEqualTo("과목");
        assertThat(totalQuantity.getTotalQuantityAmount()).isEqualTo(100);
        assertThat(totalQuantity.getUnit()).isEqualTo("문제");
    }

    @Test
    void 세션_총량의_세션_계획은_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantity.create(
                        1L,
                        null,
                        "수학",
                        "과목",
                        100,
                        "문제",
                        null
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 세션_총량의_계획량은_음수일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantity.create(
                        1L,
                        VALID_SESSIONAL_PLAN,
                        "수학",
                        "과목",
                        -1,
                        "문제",
                        null
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(VALUE_NOT_POSITIVE);
    }

    @Test
    void 세션_총량의_과목명은_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantity.create(
                        1L,
                        VALID_SESSIONAL_PLAN,
                        null,
                        "과목",
                        100,
                        "문제",
                        null
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 세션_총량의_과목명은_공백일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantity.create(
                        1L,
                        VALID_SESSIONAL_PLAN,
                        " ",
                        "과목",
                        100,
                        "문제",
                        null
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 세션_총량의_과목명은_100자를_초과할_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantity.create(
                        1L,
                        VALID_SESSIONAL_PLAN,
                        "가".repeat(101),
                        "과목",
                        100,
                        "문제",
                        null
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }

    @Test
    void 세션_총량의_과목_카테고리는_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantity.create(
                        1L,
                        VALID_SESSIONAL_PLAN,
                        "수학",
                        null,
                        100,
                        "문제",
                        null
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 세션_총량의_과목_카테고리는_공백일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantity.create(
                        1L,
                        VALID_SESSIONAL_PLAN,
                        "수학",
                        " ",
                        100,
                        "문제",
                        null
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 세션_총량의_과목_카테고리는_10자를_초과할_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantity.create(
                        1L,
                        VALID_SESSIONAL_PLAN,
                        "수학",
                        "가".repeat(11),
                        100,
                        "문제",
                        null
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }

    @Test
    void 세션_총량의_계획량_단위는_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantity.create(
                        1L,
                        VALID_SESSIONAL_PLAN,
                        "수학",
                        "과목",
                        100,
                        null,
                        null
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 세션_총량의_계획량_단위는_공백일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantity.create(
                        1L,
                        VALID_SESSIONAL_PLAN,
                        "수학",
                        "과목",
                        100,
                        " ",
                        null
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 세션_총량의_계획량_단위는_10자를_초과할_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantity.create(
                        1L,
                        VALID_SESSIONAL_PLAN,
                        "수학",
                        "과목",
                        100,
                        "가".repeat(11),
                        null
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }

    @Test
    void 세션_총량의_메모는_25자를_초과할_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantity.create(
                        1L,
                        VALID_SESSIONAL_PLAN,
                        "수학",
                        "과목",
                        100,
                        "문제",
                        "가".repeat(26)
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }
}
