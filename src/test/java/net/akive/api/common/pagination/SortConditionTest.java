package net.akive.api.common.pagination;

import org.junit.jupiter.api.Test;

import static net.akive.api.common.pagination.SortDirection.ASC;
import static net.akive.api.common.pagination.SortDirection.DESC;
import static net.akive.api.common.pagination.fixture.PaginationFixture.PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SortConditionTest {

    @Test
    void 정렬_조건을_생성한다() {
        SortCondition sortCondition = new SortCondition(PROPERTY, DESC);

        assertThat(sortCondition.property()).isEqualTo(PROPERTY);
        assertThat(sortCondition.sortDirection()).isEqualTo(DESC);
    }

    @Test
    void 정렬_방향이_null이면_오름차순을_기본값으로_사용한다() {
        SortCondition sortCondition = new SortCondition(PROPERTY, null);

        assertThat(sortCondition.property()).isEqualTo(PROPERTY);
        assertThat(sortCondition.sortDirection()).isEqualTo(ASC);
    }

    @Test
    void 정렬_속성의_앞뒤_공백을_제거한다() {
        SortCondition sortCondition = new SortCondition(" " + PROPERTY + " ", DESC);

        assertThat(sortCondition.property()).isEqualTo(PROPERTY);
        assertThat(sortCondition.sortDirection()).isEqualTo(DESC);
    }

    @Test
    void 정렬_속성만으로_오름차순_정렬_조건을_생성한다() {
        SortCondition sortCondition = SortCondition.of(PROPERTY);

        assertThat(sortCondition.property()).isEqualTo(PROPERTY);
        assertThat(sortCondition.sortDirection()).isEqualTo(ASC);
    }

    @Test
    void 정렬_속성이_null이면_정렬_조건을_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> SortCondition.of(null))
                .withMessage("Sort property must not be blank.");
    }

    @Test
    void 정렬_속성이_공백이면_정렬_조건을_생성할_수_없다() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> SortCondition.of("   "))
                .withMessage("Sort property must not be blank.");
    }
}
