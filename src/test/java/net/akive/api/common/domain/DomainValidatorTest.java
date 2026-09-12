package net.akive.api.common.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationError;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DomainValidatorTest {

    @Test
    void requireNonNull은_값이_null이_아니면_해당_값을_반환한다() {
        String value = "value";

        String validatedValue = requireNonNull(value);

        assertThat(validatedValue).isEqualTo(value);
    }

    @Test
    void requireNonNull은_값이_null이면_필수_값_누락_예외를_던진다() {
        assertBusinessRuleViolation(() -> requireNonNull(null), REQUIRED_VALUE_MISSING);
    }

    @Test
    void requireNotBlank는_값이_공백이_아니면_앞뒤_공백을_제거한_값을_반환한다() {
        String validatedValue = requireNotBlank(" value ");

        assertThat(validatedValue).isEqualTo("value");
    }

    @Test
    void requireNotBlank는_값이_공백이면_빈_값_예외를_던진다() {
        assertBusinessRuleViolation(() -> requireNotBlank("   "), BLANK_VALUE);
    }

    @Test
    void requireNotEmpty는_컬렉션이_비어있지_않으면_해당_컬렉션을_반환한다() {
        List<String> values = List.of("value");

        List<String> validatedValues = requireNotEmpty(values);

        assertThat(validatedValues).isEqualTo(values);
    }

    @Test
    void requireNotEmpty는_컬렉션이_비어있으면_빈_컬렉션_예외를_던진다() {
        assertBusinessRuleViolation(() -> requireNotEmpty(List.of()), EMPTY_COLLECTION);
    }

    @Test
    void requireMaxLength는_값의_길이가_최대_길이_이하면_해당_값을_반환한다() {
        String value = "abc";

        String validatedValue = requireMaxLength(value, 3);

        assertThat(validatedValue).isEqualTo(value);
    }

    @Test
    void requireMaxLength는_값의_길이가_최대_길이를_초과하면_최대_길이_초과_예외를_던진다() {
        assertBusinessRuleViolation(() -> requireMaxLength("abcd", 3), MAX_LENGTH_EXCEEDED);
    }

    @Test
    void requireMinLength는_값의_길이가_최소_길이_이상이면_해당_값을_반환한다() {
        String value = "abc";

        String validatedValue = requireMinLength(value, 3);

        assertThat(validatedValue).isEqualTo(value);
    }

    @Test
    void requireMinLength는_값의_길이가_최소_길이보다_짧으면_최소_길이_미달_예외를_던진다() {
        assertBusinessRuleViolation(() -> requireMinLength("ab", 3), MIN_LENGTH_NOT_MET);
    }

    @Test
    void requireMaxLengthIfPresent는_값이_null이면_null을_반환한다() {
        String validatedValue = requireMaxLengthIfPresent(null, 3);

        assertThat(validatedValue).isNull();
    }

    @Test
    void requireMaxLengthIfPresent는_값이_있고_최대_길이를_초과하면_최대_길이_초과_예외를_던진다() {
        assertBusinessRuleViolation(() -> requireMaxLengthIfPresent("abcd", 3), MAX_LENGTH_EXCEEDED);
    }

    @Test
    void requireMatches는_값이_패턴과_일치하면_해당_값을_반환한다() {
        String value = "123";

        String validatedValue = requireMatches(value, Pattern.compile("\\d+"));

        assertThat(validatedValue).isEqualTo(value);
    }

    @Test
    void requireMatches는_값이_패턴과_일치하지_않으면_형식_예외를_던진다() {
        assertBusinessRuleViolation(() -> requireMatches("abc", Pattern.compile("\\d+")), INVALID_FORMAT);
    }

    @Test
    void requirePositive는_Integer_값이_양수이면_해당_값을_반환한다() {
        Integer value = 1;

        Integer validatedValue = requirePositive(value);

        assertThat(validatedValue).isEqualTo(value);
    }

    @Test
    void requirePositive는_Integer_값이_양수가_아니면_양수_예외를_던진다() {
        assertBusinessRuleViolation(() -> requirePositive(0), VALUE_NOT_POSITIVE);
    }

    @Test
    void requirePositive는_Long_값이_양수이면_해당_값을_반환한다() {
        Long value = 1L;

        Long validatedValue = requirePositive(value);

        assertThat(validatedValue).isEqualTo(value);
    }

    @Test
    void requirePositive는_Long_값이_양수가_아니면_양수_예외를_던진다() {
        assertBusinessRuleViolation(() -> requirePositive(0L), VALUE_NOT_POSITIVE);
    }

    @Test
    void requireNonNegative는_Integer_값이_음수가_아니면_해당_값을_반환한다() {
        Integer value = 0;

        Integer validatedValue = requireNonNegative(value);

        assertThat(validatedValue).isEqualTo(value);
    }

    @Test
    void requireNonNegative는_Integer_값이_음수이면_음수_예외를_던진다() {
        assertBusinessRuleViolation(() -> requireNonNegative(-1), VALUE_NEGATIVE);
    }

    @Test
    void requireNonNegative는_Long_값이_음수가_아니면_해당_값을_반환한다() {
        Long value = 0L;

        Long validatedValue = requireNonNegative(value);

        assertThat(validatedValue).isEqualTo(value);
    }

    @Test
    void requireNonNegative는_Long_값이_음수이면_음수_예외를_던진다() {
        assertBusinessRuleViolation(() -> requireNonNegative(-1L), VALUE_NEGATIVE);
    }

    @Test
    void requireNonNegativeIfPresent는_값이_null이면_null을_반환한다() {
        Long validatedValue = requireNonNegativeIfPresent(null);

        assertThat(validatedValue).isNull();
    }

    @Test
    void requireNonNegativeIfPresent는_값이_있고_음수이면_음수_예외를_던진다() {
        assertBusinessRuleViolation(() -> requireNonNegativeIfPresent(-1L), VALUE_NEGATIVE);
    }

    @Test
    void requireStartNotAfterEnd는_LocalDate_시작일이_종료일보다_이후가_아니면_예외가_발생하지_않는다() {
        LocalDate date = LocalDate.of(2026, 9, 13);

        requireStartNotAfterEnd(date, date);
    }

    @Test
    void requireStartNotAfterEnd는_LocalDate_시작일이_종료일보다_이후면_날짜_범위_예외를_던진다() {
        LocalDate endOn = LocalDate.of(2026, 9, 13);
        LocalDate startOn = endOn.plusDays(1);

        assertBusinessRuleViolation(() -> requireStartNotAfterEnd(startOn, endOn), INVALID_DATE_RANGE);
    }

    @Test
    void requireStartNotAfterEnd는_LocalDateTime_시작일시가_종료일시보다_이후가_아니면_예외가_발생하지_않는다() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 9, 13, 9, 0);

        requireStartNotAfterEnd(dateTime, dateTime);
    }

    @Test
    void requireStartNotAfterEnd는_LocalDateTime_시작일시가_종료일시보다_이후면_날짜_범위_예외를_던진다() {
        LocalDateTime endOn = LocalDateTime.of(2026, 9, 13, 9, 0);
        LocalDateTime startOn = endOn.plusSeconds(1);

        assertBusinessRuleViolation(() -> requireStartNotAfterEnd(startOn, endOn), INVALID_DATE_RANGE);
    }

    private void assertBusinessRuleViolation(Runnable runnable, BusinessRuleViolationError error) {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(runnable::run)
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(error);
    }
}
