package net.akive.api.learner.adapter.persistence;

import net.akive.api.learner.application.required.LearnerInsightRepository;
import net.akive.api.learner.domain.LearnerInsight;
import net.akive.api.learner.domain.LearnerInsightEvidence;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Set;

import static net.akive.api.learner.domain.EvidenceSourceType.COACHING_MESSAGE;
import static net.akive.api.learner.domain.EvidenceStance.SUPPORTING;
import static net.akive.api.learner.domain.InsightConfidence.LOW;
import static net.akive.api.learner.domain.InsightConfidence.MEDIUM;
import static net.akive.api.learner.domain.InsightStatus.*;
import static net.akive.api.learner.domain.InsightType.BEHAVIOR_PATTERN;
import static net.akive.api.learner.domain.fixture.LearnerInsightEvidenceFixture.*;
import static net.akive.api.learner.domain.fixture.LearnerInsightFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LearnerInsightJpaAdapterTest extends RepositoryTestSupport {

    @Autowired
    LearnerInsightRepository learnerInsightRepository;

    @Test
    void 학습자_인사이트를_저장한다() {
        User savedUser = saveUser();
        LearnerInsight learnerInsight = LearnerInsight.create(
                savedUser,
                BEHAVIOR_PATTERN,
                INSIGHT_CONTENT,
                FIRST_OBSERVED_AT
        );

        LearnerInsight savedLearnerInsight = learnerInsightRepository.save(learnerInsight);

        assertThat(savedLearnerInsight.getLearnerInsightId()).isNotNull();
        assertThat(savedLearnerInsight.getUser()).isEqualTo(savedUser);
        assertThat(savedLearnerInsight.getInsightType()).isEqualTo(BEHAVIOR_PATTERN);
        assertThat(savedLearnerInsight.getContent()).isEqualTo(INSIGHT_CONTENT);
        assertThat(savedLearnerInsight.getConfidence()).isEqualTo(LOW);
        assertThat(savedLearnerInsight.getStatus()).isEqualTo(CANDIDATE);
        assertThat(savedLearnerInsight.getFirstObservedAt()).isEqualTo(FIRST_OBSERVED_AT);
        assertThat(savedLearnerInsight.getLastObservedAt()).isEqualTo(FIRST_OBSERVED_AT);
    }

    @Test
    void 학습자_인사이트의_신뢰도와_상태를_수정한다() {
        LearnerInsight savedLearnerInsight = learnerInsightRepository.save(createLearnerInsight(saveUser()));
        savedLearnerInsight.reinforce();

        learnerInsightRepository.save(savedLearnerInsight);
        flushAndClear();

        List<LearnerInsight> foundLearnerInsights = learnerInsightRepository.findAllByUserIdAndStatusIn(
                savedLearnerInsight.getUser().getUserId(),
                Set.of(ACTIVE)
        );
        assertThat(foundLearnerInsights).hasSize(1);

        LearnerInsight foundLearnerInsight = foundLearnerInsights.getFirst();

        assertThat(foundLearnerInsight.getLearnerInsightId())
                .isEqualTo(savedLearnerInsight.getLearnerInsightId());
        assertThat(foundLearnerInsight.getConfidence()).isEqualTo(MEDIUM);
        assertThat(foundLearnerInsight.getStatus()).isEqualTo(ACTIVE);
    }

    @Test
    void 학습자_인사이트_근거를_저장한다() {
        LearnerInsight savedLearnerInsight = learnerInsightRepository.save(createLearnerInsight(saveUser()));
        LearnerInsightEvidence evidence = LearnerInsightEvidence.create(
                savedLearnerInsight,
                COACHING_MESSAGE,
                SOURCE_ID,
                SUPPORTING,
                EVIDENCE_CONTENT,
                EVIDENCE_OBSERVED_AT
        );

        LearnerInsightEvidence savedEvidence = learnerInsightRepository.saveEvidence(evidence);

        assertThat(savedEvidence.getLearnerInsightEvidenceId()).isNotNull();
        assertThat(savedEvidence.getLearnerInsight()).isEqualTo(savedLearnerInsight);
        assertThat(savedEvidence.getSourceType()).isEqualTo(COACHING_MESSAGE);
        assertThat(savedEvidence.getSourceId()).isEqualTo(SOURCE_ID);
        assertThat(savedEvidence.getStance()).isEqualTo(SUPPORTING);
        assertThat(savedEvidence.getContent()).isEqualTo(EVIDENCE_CONTENT);
        assertThat(savedEvidence.getCreatedAt()).isNotNull();
    }

    @Test
    void 학습자_인사이트와_근거_출처가_같은_학습자_인사이트_근거를_중복으로_저장할_수_없다() {
        LearnerInsight savedLearnerInsight = learnerInsightRepository.save(createLearnerInsight(saveUser()));
        learnerInsightRepository.saveEvidence(createLearnerInsightEvidence(savedLearnerInsight, SOURCE_ID));

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> {
                    learnerInsightRepository.saveEvidence(
                            createLearnerInsightEvidence(savedLearnerInsight, SOURCE_ID)
                    );
                    entityManager.flush();
                });
    }

    @Test
    void 회원_식별자와_학습자_인사이트_상태가_일치하는_학습자_인사이트를_조회한다() {
        User savedUser = saveUser();
        User anotherSavedUser = userRepository.save(createUser(ANOTHER_EMAIL, ANOTHER_NICKNAME));
        LearnerInsight candidateInsight = learnerInsightRepository.save(createLearnerInsight(savedUser, CANDIDATE));
        LearnerInsight activeInsight = learnerInsightRepository.save(createLearnerInsight(savedUser, ACTIVE));
        learnerInsightRepository.save(createLearnerInsight(savedUser, SUPERSEDED));
        learnerInsightRepository.save(createLearnerInsight(anotherSavedUser, CANDIDATE));
        flushAndClear();

        List<LearnerInsight> foundLearnerInsights = learnerInsightRepository.findAllByUserIdAndStatusIn(
                savedUser.getUserId(),
                Set.of(CANDIDATE, ACTIVE)
        );

        assertThat(foundLearnerInsights).containsExactlyInAnyOrder(candidateInsight, activeInsight);
    }

    @Test
    void 회원_식별자와_학습자_인사이트_식별자와_상태가_모두_일치하는_학습자_인사이트를_조회한다() {
        User savedUser = saveUser();
        User anotherSavedUser = userRepository.save(createUser(ANOTHER_EMAIL, ANOTHER_NICKNAME));
        LearnerInsight matchingCandidateInsight =
                learnerInsightRepository.save(createLearnerInsight(savedUser, CANDIDATE));
        LearnerInsight matchingActiveInsight =
                learnerInsightRepository.save(createLearnerInsight(savedUser, ACTIVE));
        learnerInsightRepository.save(createLearnerInsight(savedUser, CANDIDATE));
        LearnerInsight differentStatusInsight =
                learnerInsightRepository.save(createLearnerInsight(savedUser, SUPERSEDED));
        LearnerInsight differentUserInsight =
                learnerInsightRepository.save(createLearnerInsight(anotherSavedUser, CANDIDATE));
        flushAndClear();

        List<LearnerInsight> foundLearnerInsights =
                learnerInsightRepository.findAllByUserIdAndLearnerInsightIdInAndStatusIn(
                        savedUser.getUserId(),
                        Set.of(
                                matchingCandidateInsight.getLearnerInsightId(),
                                matchingActiveInsight.getLearnerInsightId(),
                                differentStatusInsight.getLearnerInsightId(),
                                differentUserInsight.getLearnerInsightId()
                        ),
                        Set.of(CANDIDATE, ACTIVE)
                );

        assertThat(foundLearnerInsights)
                .containsExactlyInAnyOrder(matchingCandidateInsight, matchingActiveInsight);
    }

    @Test
    void 학습자_인사이트와_근거_출처로_학습자_인사이트_근거_존재_여부를_확인한다() {
        LearnerInsight savedLearnerInsight = learnerInsightRepository.save(createLearnerInsight(saveUser()));
        LearnerInsightEvidence evidence = LearnerInsightEvidence.create(
                savedLearnerInsight,
                COACHING_MESSAGE,
                SOURCE_ID,
                SUPPORTING,
                EVIDENCE_CONTENT,
                EVIDENCE_OBSERVED_AT
        );
        learnerInsightRepository.saveEvidence(evidence);
        flushAndClear();

        boolean exists = learnerInsightRepository.existsEvidenceByLearnerInsightIdAndSourceTypeAndSourceId(
                savedLearnerInsight.getLearnerInsightId(),
                COACHING_MESSAGE,
                SOURCE_ID
        );
        boolean doesNotExist = learnerInsightRepository.existsEvidenceByLearnerInsightIdAndSourceTypeAndSourceId(
                savedLearnerInsight.getLearnerInsightId(),
                COACHING_MESSAGE,
                SOURCE_ID + 1
        );

        assertThat(exists).isTrue();
        assertThat(doesNotExist).isFalse();
    }
}
