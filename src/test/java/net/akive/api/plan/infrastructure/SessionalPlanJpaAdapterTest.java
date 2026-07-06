package net.akive.api.plan.infrastructure;

import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.infrastructure.entity.SessionalPlanEntity;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;

class SessionalPlanJpaAdapterTest extends RepositoryTestSupport {

    @Autowired
    SessionalPlanJpaRepository sessionalPlanJpaRepository;

    @Test
    void 세셔널_플랜을_저장한다() {
        // given
        User savedUser = saveUserPort.save(User.signUpByEmail(
                "test@test.com",
                "USER@abcde",
                "password123!",
                USER
        ));
        SessionalPlan sessionalPlan = SessionalPlan.reconstruct(
                null,
                savedUser.getUserId(),
                "1회차",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 7)
        );

        // when
        SessionalPlan savedSessionalPlan = saveSessionalPlanPort.save(sessionalPlan);

        // then
        assertThat(savedSessionalPlan.getSessionalPlanId()).isNotNull();
        assertThat(savedSessionalPlan.getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(savedSessionalPlan.getSessionName()).isEqualTo("1회차");
        assertThat(savedSessionalPlan.getSessionStartOn()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(savedSessionalPlan.getSessionEndOn()).isEqualTo(LocalDate.of(2026, 7, 7));
        assertThat(sessionalPlanJpaRepository.findById(savedSessionalPlan.getSessionalPlanId()))
                .isPresent();
    }

    @Test
    void 회원의_세셔널_플랜_목록을_식별자_역순으로_조회한다() {
        // given
        User savedUser = saveUserPort.save(User.signUpByEmail(
                "test@test.com",
                "USER@abcde",
                "password123!",
                USER
        ));

        SessionalPlan firstPlan = SessionalPlan.reconstruct(
                null,
                savedUser.getUserId(),
                "1회차",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 7)
        );
        SessionalPlan secondPlan = SessionalPlan.reconstruct(
                null,
                savedUser.getUserId(),
                "2회차",
                LocalDate.of(2026, 7, 8),
                LocalDate.of(2026, 7, 14)
        );

        SessionalPlanEntity savedFirstPlan = sessionalPlanJpaRepository.save(
                SessionalPlanEntity.fromModel(firstPlan)
        );
        SessionalPlanEntity savedSecondPlan = sessionalPlanJpaRepository.save(
                SessionalPlanEntity.fromModel(secondPlan)
        );

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
    void 회원의_세셔널_플랜명이_이미_존재하는지_확인한다() {
        // given
        User savedUser = saveUserPort.save(User.signUpByEmail(
                "test@test.com",
                "USER@abcde",
                "password123!",
                USER
        ));
        User anotherUser = saveUserPort.save(User.signUpByEmail(
                "another@test.com",
                "USER@fghij",
                "password123!",
                USER
        ));

        SessionalPlan sessionalPlan = SessionalPlan.reconstruct(
                null,
                savedUser.getUserId(),
                "1회차",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 7)
        );
        sessionalPlanJpaRepository.save(SessionalPlanEntity.fromModel(sessionalPlan));

        // when & then
        assertThat(validateSessionalPlanPort.existsByUserIdAndSessionName(savedUser.getUserId(), "1회차"))
                .isTrue();
        assertThat(validateSessionalPlanPort.existsByUserIdAndSessionName(savedUser.getUserId(), "2회차"))
                .isFalse();
        assertThat(validateSessionalPlanPort.existsByUserIdAndSessionName(anotherUser.getUserId(), "1회차"))
                .isFalse();
    }
}
