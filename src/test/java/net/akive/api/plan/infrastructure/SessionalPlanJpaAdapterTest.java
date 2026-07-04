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
    void 회원의_세셔널_플랜_목록을_식별자_역순으로_조회한다() {
        // given
        User savedUser = saveUserPort.save(User.signUpByEmail(
                "test@test.com",
                "USER@abcde",
                "password123!",
                USER
        ));

        SessionalPlan firstPlan = SessionalPlan.create(
                null,
                savedUser.getUserId(),
                "1회차",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 7)
        );
        SessionalPlan secondPlan = SessionalPlan.create(
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
}
