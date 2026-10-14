package net.akive.api.learner.application.prompt;

import net.akive.api.learner.application.required.data.LearnerInsightAnalysisRequestData;
import net.akive.api.learner.domain.LearnerInsight;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.akive.api.learner.domain.fixture.LearnerInsightEvidenceFixture.ANOTHER_EVIDENCE_CONTENT;
import static net.akive.api.learner.domain.fixture.LearnerInsightEvidenceFixture.EVIDENCE_CONTENT;
import static net.akive.api.learner.domain.fixture.LearnerInsightFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

class LearnerInsightPromptFactoryTest {

    private static final String REFERENCE_CONTENT = "작은 목표부터 정하고 하나씩 실천해 보세요.";

    private final LearnerInsightPromptFactory learnerInsightPromptFactory =
            new LearnerInsightPromptFactory();

    @Test
    void LLM에_전달할_학습자_인사이트_분석_지침을_구성한다() {
        LearnerInsightAnalysisRequestData request = new LearnerInsightAnalysisRequestData(
                List.of(EVIDENCE_CONTENT),
                List.of(),
                List.of()
        );

        LearnerInsightPrompt prompt = learnerInsightPromptFactory.createAnalysis(request);

        assertThat(prompt.instructions()).contains(
                "Learning OS Akive의 학습자 분석기",
                "evidence 자료만 Insight를 생성하거나 변경하는 직접 근거로 사용할 수 있습니다.",
                "reference 자료는 evidence를 해석하기 위한 참고 정보이며 Evidence로 사용하지 않습니다.",
                "CREATE",
                "REINFORCE",
                "CONTRADICT",
                "SUPERSEDE",
                "NONE"
        );
    }

    @Test
    void Evidence_자료와_Reference_자료와_현재_학습자_인사이트로_분석_프롬프트의_입력부를_구성한다() {
        LearnerInsight currentInsight = reconstructLearnerInsight();
        LearnerInsightAnalysisRequestData request = new LearnerInsightAnalysisRequestData(
                List.of(EVIDENCE_CONTENT, ANOTHER_EVIDENCE_CONTENT),
                List.of(REFERENCE_CONTENT),
                List.of(currentInsight)
        );

        LearnerInsightPrompt prompt = learnerInsightPromptFactory.createAnalysis(request);

        assertThat(prompt.input()).isEqualTo("""
                # Evidence 자료
                <evidence index="1">
                %s
                </evidence>

                <evidence index="2">
                %s
                </evidence>

                # Reference 자료
                <reference index="1">
                %s
                </reference>

                # 현재 Learner Insight
                <insight id="%d" type="%s" confidence="%s" status="%s">
                %s
                </insight>
                """.formatted(
                EVIDENCE_CONTENT,
                ANOTHER_EVIDENCE_CONTENT,
                REFERENCE_CONTENT,
                LEARNER_INSIGHT_ID,
                currentInsight.getInsightType(),
                currentInsight.getConfidence(),
                currentInsight.getStatus(),
                INSIGHT_CONTENT
        ).strip());
    }

    @Test
    void Reference_자료와_현재_학습자_인사이트가_없으면_분석_프롬프트의_입력부에_없음으로_표시한다() {
        LearnerInsightAnalysisRequestData request = new LearnerInsightAnalysisRequestData(
                List.of(EVIDENCE_CONTENT),
                List.of(),
                List.of()
        );

        LearnerInsightPrompt prompt = learnerInsightPromptFactory.createAnalysis(request);

        assertThat(prompt.input()).isEqualTo("""
                # Evidence 자료
                <evidence index="1">
                %s
                </evidence>

                # Reference 자료
                없음

                # 현재 Learner Insight
                없음
                """.formatted(EVIDENCE_CONTENT).strip());
    }
}
