package net.akive.api.common.pagination;

import org.junit.jupiter.api.Test;

import static net.akive.api.common.pagination.fixture.PaginationFixture.CURSOR;
import static net.akive.api.common.pagination.fixture.PaginationFixture.CURSOR_PAGE_SIZE;
import static net.akive.api.common.pagination.fixture.PaginationFixture.DEFAULT_CURSOR_PAGE_SIZE;
import static net.akive.api.common.pagination.fixture.PaginationFixture.MAX_PAGE_SIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CursorConditionTest {

    @Test
    void 커서와_페이지_크기로_커서_페이지_조건을_생성한다() {
        CursorCondition<Long> cursorCondition = CursorCondition.of(CURSOR, CURSOR_PAGE_SIZE);

        assertThat(cursorCondition.cursor()).isEqualTo(CURSOR);
        assertThat(cursorCondition.size()).isEqualTo(CURSOR_PAGE_SIZE);
    }

    @Test
    void 커서가_null이어도_커서_페이지_조건을_생성할_수_있다() {
        CursorCondition<Long> cursorCondition = CursorCondition.of(null, CURSOR_PAGE_SIZE);

        assertThat(cursorCondition.cursor()).isNull();
        assertThat(cursorCondition.size()).isEqualTo(CURSOR_PAGE_SIZE);
    }

    @Test
    void 페이지_크기가_null이면_기본_페이지_크기를_사용한다() {
        CursorCondition<Long> cursorCondition = CursorCondition.of(CURSOR, null);

        assertThat(cursorCondition.cursor()).isEqualTo(CURSOR);
        assertThat(cursorCondition.size()).isEqualTo(DEFAULT_CURSOR_PAGE_SIZE);
    }

    @Test
    void 페이지_크기가_양수가_아니면_커서_페이지_조건을_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> CursorCondition.of(CURSOR, 0))
                .withMessage("Size must be positive.");
    }

    @Test
    void 페이지_크기가_최대값보다_크면_커서_페이지_조건을_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> CursorCondition.of(CURSOR, MAX_PAGE_SIZE + 1))
                .withMessage("Size must not exceed " + MAX_PAGE_SIZE + ".");
    }
}
