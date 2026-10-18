package net.akive.api.common.pagination;

import org.junit.jupiter.api.Test;

import static net.akive.api.common.pagination.fixture.PaginationFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PageInfoTest {

    @Test
    void 페이지_정보를_생성한다() {
        PageInfo pageInfo = new PageInfo(
                DEFAULT_PAGE,
                DEFAULT_PAGE_SIZE,
                TOTAL_ELEMENT,
                TOTAL_PAGE,
                true,
                true
        );

        assertThat(pageInfo.page()).isEqualTo(DEFAULT_PAGE);
        assertThat(pageInfo.size()).isEqualTo(DEFAULT_PAGE_SIZE);
        assertThat(pageInfo.totalElement()).isEqualTo(TOTAL_ELEMENT);
        assertThat(pageInfo.totalPage()).isEqualTo(TOTAL_PAGE);
        assertThat(pageInfo.first()).isTrue();
        assertThat(pageInfo.last()).isTrue();
    }

    @Test
    void 페이지_번호가_음수이면_페이지_정보를_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new PageInfo(
                        -1,
                        DEFAULT_PAGE_SIZE,
                        TOTAL_ELEMENT,
                        TOTAL_PAGE,
                        false,
                        true
                ))
                .withMessage("Page must not be negative.");
    }

    @Test
    void 페이지_크기가_양수가_아니면_페이지_정보를_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new PageInfo(
                        DEFAULT_PAGE,
                        0,
                        TOTAL_ELEMENT,
                        TOTAL_PAGE,
                        true,
                        true
                ))
                .withMessage("Size must be positive.");
    }

    @Test
    void 전체_요소_수가_음수이면_페이지_정보를_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new PageInfo(
                        DEFAULT_PAGE,
                        DEFAULT_PAGE_SIZE,
                        -1,
                        TOTAL_PAGE,
                        true,
                        true
                ))
                .withMessage("Total element must not be negative.");
    }

    @Test
    void 전체_페이지_수가_음수이면_페이지_정보를_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new PageInfo(
                        DEFAULT_PAGE,
                        DEFAULT_PAGE_SIZE,
                        TOTAL_ELEMENT,
                        -1,
                        true,
                        true
                ))
                .withMessage("Total page must not be negative.");
    }
}
