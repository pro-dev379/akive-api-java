package net.akive.api.plan.adapter.persistence.sessionalplan;

import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.value.SessionPeriod;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

class SessionalPlanJpaAdapterTest extends RepositoryTestSupport {

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
        assertThat(foundSessionalPlan.getSessionPeriod()).isEqualTo(sessionalPlan.getSessionPeriod());
    }

    @Test
    void 세셔널_플랜_식별자와_회원_식별자로_조회한다() {
        SessionalPlan savedSessionalPlan = saveSessionalPlan();
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
    void 회원_식별자로_세셔널_플랜을_식별자_내림차순으로_조회한다() {
        User savedUser = saveUser();
        SessionalPlan firstSessionalPlan = sessionalPlanRepository.save(
                createSessionalPlan(savedUser, SESSION_NAME)
        );
        SessionalPlan secondSessionalPlan = sessionalPlanRepository.save(
                createSessionalPlan(savedUser, ANOTHER_SESSION_NAME)
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
    void 회원_식별자로_세션_기간_목록을_날짜순으로_조회한다() {
        User savedUser = saveUser();
        SessionPeriod firstSessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 12)
        );
        SessionPeriod secondSessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 12)
        );
        sessionalPlanRepository.save(SessionalPlan.create(
                savedUser,
                SESSION_NAME,
                firstSessionPeriod,
                CURRENT_KOREAN_DATE
        ));
        sessionalPlanRepository.save(SessionalPlan.create(
                savedUser,
                ANOTHER_SESSION_NAME,
                secondSessionPeriod,
                CURRENT_KOREAN_DATE
        ));
        flushAndClear();

        List<SessionPeriod> sessionPeriods =
                sessionalPlanRepository.findSessionPeriodsByUserId(savedUser.getUserId());

        assertThat(sessionPeriods).containsExactly(firstSessionPeriod, secondSessionPeriod);
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
        SessionalPlan sessionalPlan = savedTotalQuantity.getSessionalPlan();
        flushAndClear();

        sessionalPlanRepository.delete(sessionalPlan);
        flushAndClear();

        assertThat(sessionalPlanRepository.findBySessionalPlanIdAndUserId(sessionalPlanId, userId)).isEmpty();
        assertThat(totalQuantityRepository.countBySessionalPlanId(sessionalPlanId)).isZero();
    }

    @Test
    void 회원의_진행_중인_세셔널_플랜_개수를_조회한다() {
        SessionalPlan savedSessionalPlan = saveSessionalPlan(CURRENT_KOREAN_DATE);
        flushAndClear();

        long count = sessionalPlanRepository.countInProgressByUserId(
                savedSessionalPlan.getUser().getUserId(),
                CURRENT_KOREAN_DATE
        );

        assertThat(count).isEqualTo(1);
    }
}
