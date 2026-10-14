package net.akive.api.coaching.application.prompt;

import org.junit.jupiter.api.Test;

import java.util.List;

import static net.akive.api.coaching.application.prompt.fixture.CoachingPromptCompositionFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

class CoachingPromptFactoryTest {

    private final CoachingPromptFactory coachingPromptFactory = new CoachingPromptFactory();

    @Test
    void 학습자_컨텍스트_없이_코칭_프롬프트를_생성한다() {
        CoachingPromptComposition composition = CoachingPromptComposition.from(MESSAGE);

        CoachingPrompt coachingPrompt = coachingPromptFactory.createCoaching(composition);

        assertThat(coachingPrompt.instructions()).contains("Learning OS Akive", "코치");
        assertThat(coachingPrompt.input()).isEqualTo("""
                # 회원 메시지
                <message>
                %s
                </message>
                """.formatted(composition.message()).strip());
    }

    @Test
    void 학습자_컨텍스트와_함께_코칭_프롬프트를_생성한다() {
        CoachingPromptComposition.ContextSection contextSection = createContextSection();
        CoachingPromptComposition composition = new CoachingPromptComposition(MESSAGE, List.of(contextSection));

        CoachingPrompt coachingPrompt = coachingPromptFactory.createCoaching(composition);

        assertThat(coachingPrompt.input()).isEqualTo("""
                # 참고할 학습자 컨텍스트
                <contextSection name="%s">
                %s
                </contextSection>

                # 회원 메시지
                <message>
                %s
                </message>
                """.formatted(
                        contextSection.name(),
                        contextSection.content(),
                        composition.message()
                ).strip());
    }

    @Test
    void 코칭_대화_제목_프롬프트를_생성한다() {
        String message = "학습 계획을 세우고 싶어요.";

        CoachingPrompt coachingPrompt = coachingPromptFactory.createConversationTitle(message);

        assertThat(coachingPrompt.instructions()).contains("대화 제목", "20자 이내");
        assertThat(coachingPrompt.input()).isEqualTo(message);
    }
}
