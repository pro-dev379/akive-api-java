package net.akive.api.plan.domain;

import net.akive.api.common.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.BLANK_VALUE;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.MAX_LENGTH_EXCEEDED;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.REQUIRED_VALUE_MISSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SubjectTest {

    @Test
    void 과목을_생성한다() {
        // when
        Subject subject = new Subject("수학", "과목");

        // then
        assertThat(subject.name()).isEqualTo("수학");
        assertThat(subject.category()).isEqualTo("과목");
    }

    @Test
    void 과목명은_앞뒤_공백을_제거한다() {
        // when
        Subject subject = new Subject("  수학  ", "과목");

        // then
        assertThat(subject.name()).isEqualTo("수학");
    }

    @Test
    void 카테고리는_앞뒤_공백을_제거한다() {
        // when
        Subject subject = new Subject("수학", "  과목  ");

        // then
        assertThat(subject.category()).isEqualTo("과목");
    }

    @Test
    void 과목명은_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new Subject(null, "과목"))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 과목명은_공백일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new Subject(" ", "과목"))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 과목명은_100자일_수_있다() {
        // given
        String name = "가".repeat(100);

        // when
        Subject subject = new Subject(name, "과목");

        // then
        assertThat(subject.name()).isEqualTo(name);
    }

    @Test
    void 과목명은_100자를_초과할_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new Subject("가".repeat(101), "과목"))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }

    @Test
    void 카테고리는_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new Subject("수학", null))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 카테고리는_공백일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new Subject("수학", " "))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 카테고리는_10자일_수_있다() {
        // given
        String category = "가".repeat(10);

        // when
        Subject subject = new Subject("수학", category);

        // then
        assertThat(subject.category()).isEqualTo(category);
    }

    @Test
    void 카테고리는_10자를_초과할_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new Subject("수학", "가".repeat(11)))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }
}