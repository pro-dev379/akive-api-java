package net.akive.api.common.pagination;

import org.junit.jupiter.api.Test;

import static net.akive.api.common.pagination.fixture.PaginationFixture.CONTENTS;
import static net.akive.api.common.pagination.fixture.PaginationFixture.CURSOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CursorResultTest {

    @Test
    void 커서_페이지_결과를_생성한다() {
        CursorResult<String, Long> cursorResult = CursorResult.of(CURSOR, true, CONTENTS);

        assertThat(cursorResult.cursorInfo().nextCursor()).isEqualTo(CURSOR);
        assertThat(cursorResult.cursorInfo().hasNext()).isTrue();
        assertThat(cursorResult.contents()).containsExactlyElementsOf(CONTENTS);
    }

    @Test
    void 커서_페이지_정보가_null이면_커서_페이지_결과를_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new CursorResult<>(null, CONTENTS))
                .withMessage("Cursor info must not be null.");
    }

    @Test
    void 페이지_내용이_null이면_커서_페이지_결과를_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> CursorResult.of(CURSOR, true, null))
                .withMessage("Contents must not be null.");
    }
}
