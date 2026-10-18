package net.akive.api.common.pagination;

import org.junit.jupiter.api.Test;

import static net.akive.api.common.pagination.fixture.PaginationFixture.CURSOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CursorInfoTest {

    @Test
    void 다음_커서와_다음_페이지_존재_여부로_커서_페이지_정보를_생성한다() {
        CursorInfo<Long> cursorInfo = new CursorInfo<>(CURSOR, true);

        assertThat(cursorInfo.nextCursor()).isEqualTo(CURSOR);
        assertThat(cursorInfo.hasNext()).isTrue();
    }

    @Test
    void 다음_페이지가_없으면_다음_커서가_null이어도_커서_페이지_정보를_생성할_수_있다() {
        CursorInfo<Long> cursorInfo = new CursorInfo<>(null, false);

        assertThat(cursorInfo.nextCursor()).isNull();
        assertThat(cursorInfo.hasNext()).isFalse();
    }

    @Test
    void 다음_페이지가_있는데_다음_커서가_null이면_커서_페이지_정보를_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new CursorInfo<>(null, true))
                .withMessage("Next cursor must not be null when next content exists.");
    }
}
