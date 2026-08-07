package net.akive.api.plan.adapter.persistence;

import net.akive.api.common.domain.Email;
import net.akive.api.plan.application.required.dto.SessionalPlanTotalQuantityData;
import net.akive.api.plan.application.required.dto.SessionalPlanWeeklyQuantityData;
import net.akive.api.plan.domain.SessionPeriod;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.domain.Subject;
import net.akive.api.plan.domain.TotalQuantityAmount;
import net.akive.api.plan.domain.TotalQuantityPlan;
import net.akive.api.plan.domain.TotalQuantityPlanFactory;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class SessionalPlanJpaAdapterTest extends RepositoryTestSupport {

    private final TotalQuantityPlanFactory totalQuantityPlanFactory = new TotalQuantityPlanFactory();

    @Test
    void 세셔널_플랜을_저장한다() {
        // given
        User savedUser = saveUserPort.save(User.signUpByEmail(
                new Email("test@test.com"),
                "USER@abcde",
                "password123!",
                USER
        ));
        SessionalPlan sessionalPlan = SessionalPlan.create(
                null,
                savedUser.getUserId(),
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 7)
                )
        );

        // when
        SessionalPlan savedSessionalPlan = saveSessionalPlanPort.save(sessionalPlan);

        // then
        assertThat(savedSessionalPlan.getSessionalPlanId()).isNotNull();
        assertThat(savedSessionalPlan.getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(savedSessionalPlan.getSessionName()).isEqualTo("1회차");
        assertThat(savedSessionalPlan.getSessionPeriod()).isEqualTo(
                new SessionPeriod(
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 7)
                )
        );
    }

    @Test
    void 회원의_세셔널_플랜_목록을_식별자_역순으로_조회한다() {
        // given
        User savedUser = saveUserPort.save(User.signUpByEmail(
                new Email("test@test.com"),
                "USER@abcde",
                "password123!",
                USER
        ));

        SessionalPlan firstPlan = SessionalPlan.create(
                null,
                savedUser.getUserId(),
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 7)
                )
        );
        SessionalPlan secondPlan = SessionalPlan.create(
                null,
                savedUser.getUserId(),
                "2회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 14)
                )
        );

        SessionalPlan savedFirstPlan = saveSessionalPlanPort.save(firstPlan);
        SessionalPlan savedSecondPlan = saveSessionalPlanPort.save(secondPlan);

        // when
        List<SessionalPlan> sessionalPlans = loadSessionalPlanPort
                .loadAllByUserIdOrderBySessionalPlanIdDesc(savedUser.getUserId());

        // then
        assertThat(sessionalPlans)
                .extracting(SessionalPlan::getSessionalPlanId)
                .containsExactly(
                        savedSecondPlan.getSessionalPlanId(),
                        savedFirstPlan.getSessionalPlanId()
                );
        assertThat(sessionalPlans)
                .extracting(SessionalPlan::getSessionName)
                .containsExactly("2회차", "1회차");
    }

    @Test
    void 회원의_세셔널_플랜을_조회한다() {
        // given
        User savedUser = saveUserPort.save(User.signUpByEmail(
                new Email("test@test.com"),
                "USER@abcde",
                "password123!",
                USER
        ));
        SessionalPlan sessionalPlan = SessionalPlan.create(
                null,
                savedUser.getUserId(),
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 7)
                )
        );
        SessionalPlan savedSessionalPlan = saveSessionalPlanPort.save(sessionalPlan);

        // when
        SessionalPlan loadedSessionalPlan = loadSessionalPlanPort
                .loadBySessionalPlanIdAndUserId(
                        savedSessionalPlan.getSessionalPlanId(),
                        savedUser.getUserId()
                )
                .orElseThrow();

        // then
        assertThat(loadedSessionalPlan.getSessionalPlanId()).isEqualTo(savedSessionalPlan.getSessionalPlanId());
        assertThat(loadedSessionalPlan.getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(loadedSessionalPlan.getSessionName()).isEqualTo("1회차");
        assertThat(loadedSessionalPlan.getSessionPeriod()).isEqualTo(
                new SessionPeriod(
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 7)
                )
        );
    }

    @Test
    void 회원의_세셔널_플랜_세션_기간을_조회한다() {
        // given
        User savedUser = saveUserPort.save(User.signUpByEmail(
                new Email("test@test.com"),
                "USER@abcde",
                "password123!",
                USER
        ));
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 7)
        );
        SessionalPlan sessionalPlan = SessionalPlan.create(
                null,
                savedUser.getUserId(),
                "1회차",
                sessionPeriod
        );
        SessionalPlan savedSessionalPlan = saveSessionalPlanPort.save(sessionalPlan);

        // when
        SessionPeriod loadedSessionPeriod = loadSessionalPlanPort
                .loadSessionPeriodBySessionalPlanIdAndUserId(
                        savedSessionalPlan.getSessionalPlanId(),
                        savedUser.getUserId()
                )
                .orElseThrow();

        // then
        assertThat(loadedSessionPeriod).isEqualTo(sessionPeriod);
    }

    @Test
    void 세셔널_플랜의_세션_총량을_조회한다() {
        // given
        User savedUser = saveUserPort.save(User.signUpByEmail(
                new Email("test@test.com"),
                "USER@abcde",
                "password123!",
                USER
        ));
        SessionalPlan savedSessionalPlan = saveSessionalPlanPort.save(SessionalPlan.create(
                null,
                savedUser.getUserId(),
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        ));
        saveTotalQuantity(savedSessionalPlan, "수학", 100);
        Long savedEnglishTotalQuantityId = saveTotalQuantity(savedSessionalPlan, "영어", 200);

        // when
        SessionalPlanTotalQuantityData data = loadTotalQuantityPort
                .loadSessionalPlanTotalQuantityBySessionalPlanIdAndTotalQuantityId(
                        savedSessionalPlan.getSessionalPlanId(),
                        savedEnglishTotalQuantityId,
                        savedSessionalPlan.getSessionPeriod().splitIntoWeeks()
                )
                .orElseThrow();

        // then
        assertThat(data.totalQuantityId()).isEqualTo(savedEnglishTotalQuantityId);
        assertThat(data.sessionalPlanId()).isEqualTo(savedSessionalPlan.getSessionalPlanId());
        assertThat(data.subject()).isEqualTo(new Subject("영어", "과목"));
        assertThat(data.totalQuantityAmount()).isEqualTo(TotalQuantityAmount.of(200, "문제", null));
        assertThat(data.weeklyQuantities())
                .hasSize(2)
                .extracting(
                        SessionalPlanWeeklyQuantityData::totalQuantityId,
                        weeklyQuantity -> weeklyQuantity.sessionWeek().weekNum(),
                        weeklyQuantity -> weeklyQuantity.weeklyQuantityAmount().quantityAmount(),
                        weeklyQuantity -> weeklyQuantity.weeklyQuantityAmount().completedQuantityAmount(),
                        weeklyQuantity -> weeklyQuantity.weeklyQuantityAmount().delayedQuantityAmount()
                )
                .containsExactly(
                        tuple(savedEnglishTotalQuantityId, 1, 200, 0, 200),
                        tuple(savedEnglishTotalQuantityId, 2, 0, 0, 0)
                );
    }

    @Test
    void 세셔널_플랜에_존재하지_않는_세션_총량은_조회되지_않는다() {
        // given
        User savedUser = saveUserPort.save(User.signUpByEmail(
                new Email("test@test.com"),
                "USER@abcde",
                "password123!",
                USER
        ));
        SessionalPlan savedSessionalPlan = saveSessionalPlanPort.save(SessionalPlan.create(
                null,
                savedUser.getUserId(),
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        ));

        // when & then
        assertThat(loadTotalQuantityPort.loadSessionalPlanTotalQuantityBySessionalPlanIdAndTotalQuantityId(
                savedSessionalPlan.getSessionalPlanId(),
                999L,
                savedSessionalPlan.getSessionPeriod().splitIntoWeeks()
        )).isEmpty();
    }

    @Test
    void 다른_회원의_세셔널_플랜_세션_기간은_조회되지_않는다() {
        // given
        User savedUser = saveUserPort.save(User.signUpByEmail(
                new Email("test@test.com"),
                "USER@abcde",
                "password123!",
                USER
        ));
        User anotherUser = saveUserPort.save(User.signUpByEmail(
                new Email("another@test.com"),
                "USER@fghij",
                "password123!",
                USER
        ));
        SessionalPlan sessionalPlan = SessionalPlan.create(
                null,
                savedUser.getUserId(),
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 7)
                )
        );
        SessionalPlan savedSessionalPlan = saveSessionalPlanPort.save(sessionalPlan);

        // when & then
        assertThat(loadSessionalPlanPort.loadSessionPeriodBySessionalPlanIdAndUserId(
                savedSessionalPlan.getSessionalPlanId(),
                anotherUser.getUserId()
        )).isEmpty();
    }

    @Test
    void 다른_회원의_세셔널_플랜은_조회되지_않는다() {
        // given
        User savedUser = saveUserPort.save(User.signUpByEmail(
                new Email("test@test.com"),
                "USER@abcde",
                "password123!",
                USER
        ));
        User anotherUser = saveUserPort.save(User.signUpByEmail(
                new Email("another@test.com"),
                "USER@fghij",
                "password123!",
                USER
        ));
        SessionalPlan sessionalPlan = SessionalPlan.create(
                null,
                savedUser.getUserId(),
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 7)
                )
        );
        SessionalPlan savedSessionalPlan = saveSessionalPlanPort.save(sessionalPlan);

        // when & then
        assertThat(loadSessionalPlanPort.loadBySessionalPlanIdAndUserId(
                savedSessionalPlan.getSessionalPlanId(),
                anotherUser.getUserId()
        )).isEmpty();
    }

    @Test
    void 회원의_세셔널_플랜명이_이미_존재하는지_확인한다() {
        // given
        User savedUser = saveUserPort.save(User.signUpByEmail(
                new Email("test@test.com"),
                "USER@abcde",
                "password123!",
                USER
        ));
        User anotherUser = saveUserPort.save(User.signUpByEmail(
                new Email("another@test.com"),
                "USER@fghij",
                "password123!",
                USER
        ));

        SessionalPlan sessionalPlan = SessionalPlan.create(
                null,
                savedUser.getUserId(),
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 7)
                )
        );
        saveSessionalPlanPort.save(sessionalPlan);

        // when & then
        assertThat(validateSessionalPlanPort.existsByUserIdAndSessionName(savedUser.getUserId(), "1회차"))
                .isTrue();
        assertThat(validateSessionalPlanPort.existsByUserIdAndSessionName(savedUser.getUserId(), "2회차"))
                .isFalse();
        assertThat(validateSessionalPlanPort.existsByUserIdAndSessionName(anotherUser.getUserId(), "1회차"))
                .isFalse();
    }

    @Test
    void 현재_세셔널_플랜을_제외하고_회원의_세셔널_플랜명이_이미_존재하는지_확인한다() {
        // given
        User savedUser = saveUserPort.save(User.signUpByEmail(
                new Email("test@test.com"),
                "USER@abcde",
                "password123!",
                USER
        ));
        User anotherUser = saveUserPort.save(User.signUpByEmail(
                new Email("another@test.com"),
                "USER@fghij",
                "password123!",
                USER
        ));

        SessionalPlan firstPlan = SessionalPlan.create(
                null,
                savedUser.getUserId(),
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 7)
                )
        );
        SessionalPlan secondPlan = SessionalPlan.create(
                null,
                savedUser.getUserId(),
                "2회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 14)
                )
        );
        SessionalPlan anotherUserPlan = SessionalPlan.create(
                null,
                anotherUser.getUserId(),
                "2회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 14)
                )
        );

        SessionalPlan savedFirstPlan = saveSessionalPlanPort.save(firstPlan);
        SessionalPlan savedSecondPlan = saveSessionalPlanPort.save(secondPlan);
        saveSessionalPlanPort.save(anotherUserPlan);

        // when & then
        assertThat(validateSessionalPlanPort.existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                savedUser.getUserId(),
                "1회차",
                savedFirstPlan.getSessionalPlanId()
        )).isFalse();
        assertThat(validateSessionalPlanPort.existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                savedUser.getUserId(),
                "2회차",
                savedFirstPlan.getSessionalPlanId()
        )).isTrue();
        assertThat(validateSessionalPlanPort.existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                savedUser.getUserId(),
                "2회차",
                savedSecondPlan.getSessionalPlanId()
        )).isFalse();
    }

    @Test
    void 회원의_진행_중인_세셔널_플랜_개수를_조회한다() {
        // given
        User savedUser = saveUserPort.save(User.signUpByEmail(
                new Email("test@test.com"),
                "USER@abcde",
                "password123!",
                USER
        ));
        User anotherUser = saveUserPort.save(User.signUpByEmail(
                new Email("another@test.com"),
                "USER@fghij",
                "password123!",
                USER
        ));
        LocalDate baseDate = LocalDate.of(2026, 7, 12);

        saveSessionalPlanPort.save(SessionalPlan.create(
                null,
                savedUser.getUserId(),
                "종료된 회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 11)
                )
        ));
        saveSessionalPlanPort.save(SessionalPlan.create(
                null,
                savedUser.getUserId(),
                "오늘 종료 회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 5),
                        baseDate
                )
        ));
        saveSessionalPlanPort.save(SessionalPlan.create(
                null,
                savedUser.getUserId(),
                "예정 회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 13),
                        LocalDate.of(2026, 7, 20)
                )
        ));
        saveSessionalPlanPort.save(SessionalPlan.create(
                null,
                anotherUser.getUserId(),
                "다른 회원 회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 5),
                        LocalDate.of(2026, 7, 20)
                )
        ));

        // when
        int count = validateSessionalPlanPort.countInProgressByUserId(savedUser.getUserId(), baseDate);

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void 회원의_세셔널_플랜을_삭제한다() {
        // given
        User savedUser = saveUserPort.save(User.signUpByEmail(
                new Email("test@test.com"),
                "USER@abcde",
                "password123!",
                USER
        ));
        SessionalPlan sessionalPlan = SessionalPlan.create(
                null,
                savedUser.getUserId(),
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 7)
                )
        );
        SessionalPlan savedSessionalPlan = saveSessionalPlanPort.save(sessionalPlan);

        // when
        long deletedCount = deleteSessionalPlanPort.deleteBySessionalPlanIdAndUserId(
                savedSessionalPlan.getSessionalPlanId(),
                savedUser.getUserId()
        );

        // then
        assertThat(deletedCount).isEqualTo(1L);
        assertThat(loadSessionalPlanPort.loadAllByUserIdOrderBySessionalPlanIdDesc(savedUser.getUserId()))
                .isEmpty();
    }

    @Test
    void 다른_회원의_세셔널_플랜은_삭제하지_않는다() {
        // given
        User savedUser = saveUserPort.save(User.signUpByEmail(
                new Email("test@test.com"),
                "USER@abcde",
                "password123!",
                USER
        ));
        User anotherUser = saveUserPort.save(User.signUpByEmail(
                new Email("another@test.com"),
                "USER@fghij",
                "password123!",
                USER
        ));
        SessionalPlan sessionalPlan = SessionalPlan.create(
                null,
                savedUser.getUserId(),
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 7)
                )
        );
        SessionalPlan savedSessionalPlan = saveSessionalPlanPort.save(sessionalPlan);

        // when
        long deletedCount = deleteSessionalPlanPort.deleteBySessionalPlanIdAndUserId(
                savedSessionalPlan.getSessionalPlanId(),
                anotherUser.getUserId()
        );

        // then
        assertThat(deletedCount).isZero();
        assertThat(loadSessionalPlanPort.loadAllByUserIdOrderBySessionalPlanIdDesc(savedUser.getUserId()))
                .extracting(SessionalPlan::getSessionalPlanId)
                .containsExactly(savedSessionalPlan.getSessionalPlanId());
    }

    private Long saveTotalQuantity(
            SessionalPlan sessionalPlan,
            String subjectName,
            int quantityAmount
    ) {
        TotalQuantityPlan totalQuantityPlan = totalQuantityPlanFactory.create(
                sessionalPlan,
                new Subject(subjectName, "과목"),
                TotalQuantityAmount.of(quantityAmount, "문제", null)
        );

        return saveTotalQuantityPort.save(totalQuantityPlan);
    }
}
