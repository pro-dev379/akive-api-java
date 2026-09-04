package net.akive.api.plan.adapter.persistence.sessionalplan;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.value.SessionPeriod;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.exception.business.ResourceNotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.plan.domain.PlanFixture.createSessionalPlan;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SessionalPlanJpaAdapterTest extends RepositoryTestSupport {

    private static final String SESSION_NAME = "2026 1월 학습";

    @Test
    void 세셔널_플랜을_저장한다() {
        User savedUser = saveUser();
        SessionalPlan sessionalPlan = createSessionalPlan(savedUser, SESSION_NAME);

        SessionalPlan savedSessionalPlan = sessionalPlanRepository.save(sessionalPlan);
        flushAndClear();

        SessionalPlan foundSessionalPlan = sessionalPlanRepository.findBySessionalPlanIdAndUserId(
                savedSessionalPlan.getSessionalPlanId(),
                savedUser.getUserId()
        ).orElseThrow();

        assertThat(foundSessionalPlan.getSessionalPlanId()).isEqualTo(savedSessionalPlan.getSessionalPlanId());
        assertThat(foundSessionalPlan.getUser().getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(foundSessionalPlan.getSessionName()).isEqualTo(SESSION_NAME);
        assertThat(foundSessionalPlan.getSessionPeriod()).isEqualTo(createSessionalPlan(savedUser).getSessionPeriod());
    }

    @Test
    void 세셔널_플랜_식별자와_회원_식별자로_조회한다() {
        SessionalPlan savedSessionalPlan = saveSessionalPlan(SESSION_NAME);
        flushAndClear();

        SessionalPlan foundSessionalPlan = sessionalPlanRepository.findBySessionalPlanIdAndUserId(
                savedSessionalPlan.getSessionalPlanId(),
                savedSessionalPlan.getUser().getUserId()
        ).orElseThrow();

        assertThat(foundSessionalPlan.getSessionalPlanId()).isEqualTo(savedSessionalPlan.getSessionalPlanId());
        assertThat(foundSessionalPlan.getUser().getUserId()).isEqualTo(savedSessionalPlan.getUser().getUserId());
        assertThat(foundSessionalPlan.getSessionName()).isEqualTo(savedSessionalPlan.getSessionName());
        assertThat(foundSessionalPlan.getSessionPeriod()).isEqualTo(savedSessionalPlan.getSessionPeriod());
    }

    @Test
    void 세셔널_플랜_식별자와_회원_식별자로_세션_기간을_조회한다() {
        SessionalPlan savedSessionalPlan = saveSessionalPlan();
        flushAndClear();

        SessionPeriod foundSessionPeriod = sessionalPlanRepository.findSessionPeriodBySessionalPlanIdAndUserId(
                savedSessionalPlan.getSessionalPlanId(),
                savedSessionalPlan.getUser().getUserId()
        ).orElseThrow();

        assertThat(foundSessionPeriod).isEqualTo(savedSessionalPlan.getSessionPeriod());
    }

    @Test
    void 회원_식별자로_세셔널_플랜을_식별자_내림차순으로_조회한다() {
        User savedUser = saveUser();
        SessionalPlan firstSessionalPlan = sessionalPlanRepository.save(
                createSessionalPlan(savedUser, "2026 1월 학습")
        );
        SessionalPlan secondSessionalPlan = sessionalPlanRepository.save(
                createSessionalPlan(savedUser, "2026 2월 학습")
        );
        flushAndClear();

        List<SessionalPlan> sessionalPlans = sessionalPlanRepository
                .findAllByUserIdOrderBySessionalPlanIdDesc(savedUser.getUserId());

        assertThat(sessionalPlans)
                .extracting(SessionalPlan::getSessionalPlanId)
                .containsExactly(
                        secondSessionalPlan.getSessionalPlanId(),
                        firstSessionalPlan.getSessionalPlanId()
                );
    }

    @Test
    void 세셔널_플랜_존재_여부와_세션_이름_중복_여부를_조회한다() {
        SessionalPlan savedSessionalPlan = saveSessionalPlan(SESSION_NAME);
        Long userId = savedSessionalPlan.getUser().getUserId();
        Long sessionalPlanId = savedSessionalPlan.getSessionalPlanId();
        flushAndClear();

        assertThat(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId)).isTrue();
        assertThat(sessionalPlanRepository.existsByUserIdAndSessionName(userId, SESSION_NAME)).isTrue();
        assertThat(sessionalPlanRepository.existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                userId,
                SESSION_NAME,
                sessionalPlanId
        )).isFalse();
    }

    @Test
    void 세셔널_플랜을_삭제하면_세션_총량도_삭제된다() {
        TotalQuantity savedTotalQuantity = saveTotalQuantity();
        Long sessionalPlanId = savedTotalQuantity.getSessionalPlan().getSessionalPlanId();
        Long userId = savedTotalQuantity.getSessionalPlan().getUser().getUserId();
        flushAndClear();

        sessionalPlanRepository.deleteBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        flushAndClear();

        assertThat(sessionalPlanRepository.findBySessionalPlanIdAndUserId(sessionalPlanId, userId)).isEmpty();
        assertThat(totalQuantityRepository.countBySessionalPlanId(sessionalPlanId)).isZero();
    }

    @Test
    void 세셔널_플랜이_없으면_삭제에_실패하고_예외를_던진다() {
        SessionalPlan savedSessionalPlan = saveSessionalPlan();
        flushAndClear();

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> sessionalPlanRepository.deleteBySessionalPlanIdAndUserId(
                        Long.MAX_VALUE,
                        savedSessionalPlan.getUser().getUserId()
                ))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);
    }

    @Test
    void 회원의_진행_중인_세셔널_플랜_개수를_조회한다() {
        SessionalPlan savedSessionalPlan = saveSessionalPlan();
        flushAndClear();

        long count = sessionalPlanRepository.countInProgressByUserId(
                savedSessionalPlan.getUser().getUserId(),
                LocalDate.of(2026, 1, 12)
        );

        assertThat(count).isEqualTo(1);
    }
}
