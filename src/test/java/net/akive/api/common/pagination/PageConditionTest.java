package net.akive.api.common.pagination;

import org.junit.jupiter.api.Test;

import static net.akive.api.common.pagination.SortDirection.ASC;
import static net.akive.api.common.pagination.SortDirection.DESC;
import static net.akive.api.common.pagination.fixture.PaginationFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PageConditionTest {

    @Test
    void 페이지_조건을_생성한다() {
        PageCondition pageCondition = PageCondition.of(PAGE, PAGE_SIZE, PROPERTY, DESC);

        assertThat(pageCondition.getPage()).isEqualTo(PAGE);
        assertThat(pageCondition.getSize()).isEqualTo(PAGE_SIZE);
        assertThat(pageCondition.getSortCondition().property()).isEqualTo(PROPERTY);
        assertThat(pageCondition.getSortCondition().sortDirection()).isEqualTo(DESC);
    }

    @Test
    void 정렬_속성만으로_기본_페이지_조건을_생성한다() {
        PageCondition pageCondition = PageCondition.of(PROPERTY);

        assertThat(pageCondition.getPage()).isEqualTo(DEFAULT_PAGE);
        assertThat(pageCondition.getSize()).isEqualTo(DEFAULT_PAGE_SIZE);
        assertThat(pageCondition.getSortCondition().property()).isEqualTo(PROPERTY);
        assertThat(pageCondition.getSortCondition().sortDirection()).isEqualTo(ASC);
    }

    @Test
    void 페이지_번호가_null이면_기본_페이지_번호를_사용한다() {
        PageCondition pageCondition = PageCondition.of(null, PAGE_SIZE, PROPERTY);

        assertThat(pageCondition.getPage()).isEqualTo(DEFAULT_PAGE);
        assertThat(pageCondition.getSize()).isEqualTo(PAGE_SIZE);
        assertThat(pageCondition.getSortCondition().property()).isEqualTo(PROPERTY);
    }

    @Test
    void 페이지_크기가_null이면_기본_페이지_크기를_사용한다() {
        PageCondition pageCondition = PageCondition.of(PAGE, null, PROPERTY);

        assertThat(pageCondition.getPage()).isEqualTo(PAGE);
        assertThat(pageCondition.getSize()).isEqualTo(DEFAULT_PAGE_SIZE);
        assertThat(pageCondition.getSortCondition().property()).isEqualTo(PROPERTY);
    }

    @Test
    void 페이지_번호와_페이지_크기가_null이면_기본값을_사용한다() {
        PageCondition pageCondition = PageCondition.of(null, null, PROPERTY);

        assertThat(pageCondition.getPage()).isEqualTo(DEFAULT_PAGE);
        assertThat(pageCondition.getSize()).isEqualTo(DEFAULT_PAGE_SIZE);
        assertThat(pageCondition.getSortCondition().property()).isEqualTo(PROPERTY);
    }

    @Test
    void 페이지_번호가_음수이면_페이지_조건을_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PageCondition.of(-1, PAGE_SIZE, PROPERTY))
                .withMessage("Page must not be negative.");
    }

    @Test
    void 페이지_크기가_양수가_아니면_페이지_조건을_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PageCondition.of(PAGE, 0, PROPERTY))
                .withMessage("Size must be positive.");
    }

    @Test
    void 페이지_크기가_최대값보다_크면_페이지_조건을_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PageCondition.of(PAGE, MAX_PAGE_SIZE + 1, PROPERTY))
                .withMessage("Size must not exceed " + MAX_PAGE_SIZE + ".");
    }

    @Test
    void 정렬_속성이_올바르지_않으면_페이지_조건을_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PageCondition.of(PAGE, PAGE_SIZE, "   "))
                .withMessage("Sort property must not be blank.");
    }
}
