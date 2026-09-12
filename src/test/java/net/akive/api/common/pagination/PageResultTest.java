package net.akive.api.common.pagination;

import org.junit.jupiter.api.Test;

import java.util.List;

import static net.akive.api.common.pagination.fixture.PaginationFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PageResultTest {

    @Test
    void 페이징_결과를_생성한다() {
        PageResult<String> pageResult = createPageResult();

        assertThat(pageResult.page()).isEqualTo(DEFAULT_PAGE);
        assertThat(pageResult.size()).isEqualTo(CONTENTS.size());
        assertThat(pageResult.totalElement()).isEqualTo(CONTENTS.size());
        assertThat(pageResult.totalPage()).isEqualTo(1);
        assertThat(pageResult.first()).isTrue();
        assertThat(pageResult.last()).isTrue();
        assertThat(pageResult.contents()).containsExactlyElementsOf(CONTENTS);
    }

    @Test
    void 페이지_번호가_음수이면_페이징_결과를_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new PageResult<>(
                        -1,
                        DEFAULT_SIZE,
                        TOTAL_ELEMENT,
                        TOTAL_PAGE,
                        false,
                        true,
                        CONTENTS
                ))
                .withMessage("Page must not be negative.");
    }

    @Test
    void 페이지_크기가_양수가_아니면_페이징_결과를_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new PageResult<>(
                        DEFAULT_PAGE,
                        0,
                        TOTAL_ELEMENT,
                        TOTAL_PAGE,
                        true,
                        true,
                        CONTENTS
                ))
                .withMessage("Size must be positive.");
    }

    @Test
    void 전체_요소_수가_음수이면_페이징_결과를_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new PageResult<>(
                        DEFAULT_PAGE,
                        DEFAULT_SIZE,
                        -1,
                        TOTAL_PAGE,
                        true,
                        true,
                        CONTENTS
                ))
                .withMessage("Total element must not be negative.");
    }

    @Test
    void 전체_페이지_수가_음수이면_페이징_결과를_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new PageResult<>(
                        DEFAULT_PAGE,
                        DEFAULT_SIZE,
                        TOTAL_ELEMENT,
                        -1,
                        true,
                        true,
                        CONTENTS
                ))
                .withMessage("Total page must not be negative.");
    }

    @Test
    void 페이지_내용이_null이면_페이징_결과를_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new PageResult<>(
                        DEFAULT_PAGE,
                        DEFAULT_SIZE,
                        TOTAL_ELEMENT,
                        TOTAL_PAGE,
                        true,
                        true,
                        null
                ))
                .withMessage("Contents must not be null.");
    }

    @Test
    void 페이지_내용_수가_페이지_크기보다_크면_페이징_결과를_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new PageResult<>(
                        DEFAULT_PAGE,
                        1,
                        TOTAL_ELEMENT,
                        TOTAL_PAGE,
                        true,
                        false,
                        List.of("a", "b", "c", "d", "e")
                ))
                .withMessage("Contents size must not exceed page size.");
    }
}
