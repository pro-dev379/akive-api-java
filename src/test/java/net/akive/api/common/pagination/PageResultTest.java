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

        assertThat(pageResult.pageInfo().page()).isEqualTo(DEFAULT_PAGE);
        assertThat(pageResult.pageInfo().size()).isEqualTo(CONTENTS.size());
        assertThat(pageResult.pageInfo().totalElement()).isEqualTo(CONTENTS.size());
        assertThat(pageResult.pageInfo().totalPage()).isEqualTo(1);
        assertThat(pageResult.pageInfo().first()).isTrue();
        assertThat(pageResult.pageInfo().last()).isTrue();
        assertThat(pageResult.contents()).containsExactlyElementsOf(CONTENTS);
    }

    @Test
    void 페이지_정보가_null이면_페이징_결과를_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new PageResult<>(null, CONTENTS))
                .withMessage("Page info must not be null.");
    }

    @Test
    void 페이지_내용이_null이면_페이징_결과를_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PageResult.of(
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
                .isThrownBy(() -> PageResult.of(
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
