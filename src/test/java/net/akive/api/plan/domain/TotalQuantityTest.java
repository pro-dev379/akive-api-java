package net.akive.api.plan.domain;

import net.akive.api.common.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.REQUIRED_VALUE_MISSING;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.VALUE_NOT_POSITIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TotalQuantityTest {

    private static final SessionalPlan VALID_SESSIONAL_PLAN = SessionalPlan.create(
            2L,
            1L,
            "세션",
            new SessionPeriod(
                    LocalDate.of(2026, 7, 7),
                    LocalDate.of(2026, 7, 14)
            )
    );
    private static final Subject VALID_SUBJECT = new Subject("수학", "과목");
    private static final TotalQuantityAmount VALID_TOTAL_QUANTITY_AMOUNT =
            TotalQuantityAmount.of(100, "문제", "메모");

    @Test
    void 사용자의_세션_총량을_생성한다() {
        // when
        TotalQuantity totalQuantity = TotalQuantity.createForUser(
                VALID_SESSIONAL_PLAN,
                VALID_SUBJECT,
                VALID_TOTAL_QUANTITY_AMOUNT
        );

        // then
        assertThat(totalQuantity.getTotalQuantityId()).isNull();
        assertThat(totalQuantity.getSessionalPlan()).isEqualTo(VALID_SESSIONAL_PLAN);
        assertThat(totalQuantity.getSubject()).isEqualTo(VALID_SUBJECT);
        assertThat(totalQuantity.getTotalQuantityAmount()).isEqualTo(VALID_TOTAL_QUANTITY_AMOUNT);
    }

    @Test
    void 영속화된_세션_총량을_도메인_객체로_복원한다() {
        // when
        TotalQuantity totalQuantity = TotalQuantity.create(
                1L,
                VALID_SESSIONAL_PLAN,
                VALID_SUBJECT,
                VALID_TOTAL_QUANTITY_AMOUNT
        );

        // then
        assertThat(totalQuantity.getTotalQuantityId()).isEqualTo(1L);
        assertThat(totalQuantity.getSessionalPlan()).isEqualTo(VALID_SESSIONAL_PLAN);
        assertThat(totalQuantity.getSubject()).isEqualTo(VALID_SUBJECT);
        assertThat(totalQuantity.getTotalQuantityAmount()).isEqualTo(VALID_TOTAL_QUANTITY_AMOUNT);
    }

    @Test
    void 사용자_세션_총량의_세셔널_플랜은_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantity.createForUser(
                        null,
                        VALID_SUBJECT,
                        VALID_TOTAL_QUANTITY_AMOUNT
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 사용자_세션_총량의_과목은_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantity.createForUser(
                        VALID_SESSIONAL_PLAN,
                        null,
                        VALID_TOTAL_QUANTITY_AMOUNT
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 사용자_세션_총량의_계획량은_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantity.createForUser(
                        VALID_SESSIONAL_PLAN,
                        VALID_SUBJECT,
                        null
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 영속회된_세션_총량의_식별자는_양수여야_한다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantity.create(
                        0L,
                        VALID_SESSIONAL_PLAN,
                        VALID_SUBJECT,
                        VALID_TOTAL_QUANTITY_AMOUNT
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(VALUE_NOT_POSITIVE);
    }

    @Test
    void 영속화된_세션_총량의_세셔널_플랜은_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantity.create(
                        1L,
                        null,
                        VALID_SUBJECT,
                        VALID_TOTAL_QUANTITY_AMOUNT
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 영속화된_세션_총량의_과목은_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantity.create(
                        1L,
                        VALID_SESSIONAL_PLAN,
                        null,
                        VALID_TOTAL_QUANTITY_AMOUNT
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 영속화된_세션_총량의_계획량은_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantity.create(
                        1L,
                        VALID_SESSIONAL_PLAN,
                        VALID_SUBJECT,
                        null
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }
}
