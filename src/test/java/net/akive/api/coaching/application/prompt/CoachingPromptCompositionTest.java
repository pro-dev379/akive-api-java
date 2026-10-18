package net.akive.api.coaching.application.prompt;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static net.akive.api.coaching.application.prompt.fixture.CoachingPromptCompositionFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoachingPromptCompositionTest {

    @Test
    void 코칭_프롬프트_구성을_생성한다() {
        CoachingPromptComposition.ContextSection contextSection = new CoachingPromptComposition.ContextSection(
                "  " + CONTEXT_SECTION_NAME + "  ",
                "  " + CONTEXT_SECTION_CONTENT + "  "
        );

        CoachingPromptComposition composition = new CoachingPromptComposition(
                "  " + MESSAGE + "  ",
                List.of(contextSection)
        );

        assertThat(composition.message()).isEqualTo(MESSAGE);
        assertThat(composition.contextSections()).containsExactly(contextSection);
        assertThat(contextSection.name()).isEqualTo(CONTEXT_SECTION_NAME);
        assertThat(contextSection.content()).isEqualTo(CONTEXT_SECTION_CONTENT);
    }

    @Test
    void 코칭_컨텍스트_섹션_없이_메시지만으로_코칭_프롬프트를_구성한다() {
        CoachingPromptComposition composition = CoachingPromptComposition.from("  " + MESSAGE + "  ");

        assertThat(composition.message()).isEqualTo(MESSAGE);
        assertThat(composition.contextSections()).isEmpty();
    }

    @Test
    void 코칭_프롬프트_구성의_코칭_컨텍스트_섹션_목록은_외부에서_수정할_수_없다() {
        CoachingPromptComposition.ContextSection contextSection = createContextSection();
        List<CoachingPromptComposition.ContextSection> contextSections = new ArrayList<>(List.of(contextSection));
        CoachingPromptComposition composition = new CoachingPromptComposition(MESSAGE, contextSections);

        contextSections.clear();

        assertThat(composition.contextSections()).containsExactly(contextSection);
        assertThatThrownBy(() -> composition.contextSections().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
