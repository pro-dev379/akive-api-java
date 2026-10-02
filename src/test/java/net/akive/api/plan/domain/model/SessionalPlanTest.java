package net.akive.api.plan.domain.model;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.plan.domain.value.SessionPeriod;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.*;
import static net.akive.api.plan.domain.policy.PlanPolicy.SESSION_NAME_MAX_LENGTH;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SessionalPlanTest {

    @Test
    void 저장된_세셔널_플랜을_복원한다() {
        User user = createReconstructedUser();

        SessionalPlan sessionalPlan = SessionalPlan.reconstruct(
                SESSIONAL_PLAN_ID,
                user,
                SESSION_NAME,
                SESSION_PERIOD
        );

        assertThat(sessionalPlan.getSessionalPlanId()).isEqualTo(SESSIONAL_PLAN_ID);
        assertThat(sessionalPlan.getUser()).isEqualTo(user);
        assertThat(sessionalPlan.getSessionName()).isEqualTo(SESSION_NAME);
        assertThat(sessionalPlan.getSessionPeriod()).isEqualTo(SESSION_PERIOD);
    }

    @Test
    void 세셔널_플랜을_생성한다() {
        User user = createReconstructedUser();

        SessionalPlan sessionalPlan = SessionalPlan.create(
                user,
                "  " + SESSION_NAME + "  ",
                SESSION_PERIOD,
                CURRENT_KOREAN_DATE
        );

        assertThat(sessionalPlan.getSessionalPlanId()).isNull();
        assertThat(sessionalPlan.getUser()).isEqualTo(user);
        assertThat(sessionalPlan.getSessionName()).isEqualTo(SESSION_NAME);
        assertThat(sessionalPlan.getSessionPeriod()).isEqualTo(SESSION_PERIOD);
    }

    @Test
    void 세셔널_플랜_생성_시_세션_시작일이_생성_요청일보다_과거일_수_없다() {
        SessionPeriod sessionPeriod = new SessionPeriod(
                CURRENT_KOREAN_DATE.minusDays(1),
                CURRENT_KOREAN_DATE.plusDays(1)
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> SessionalPlan.create(
                        createReconstructedUser(),
                        SESSION_NAME,
                        sessionPeriod,
                        CURRENT_KOREAN_DATE
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 세셔널_플랜_생성_시_세션_종료일이_생성_요청일과_같을_수_없다() {
        SessionPeriod sessionPeriod = new SessionPeriod(
                CURRENT_KOREAN_DATE,
                CURRENT_KOREAN_DATE.plusDays(1)
        );
        LocalDate currentDate = CURRENT_KOREAN_DATE.plusDays(1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> SessionalPlan.create(
                        createReconstructedUser(),
                        SESSION_NAME,
                        sessionPeriod,
                        currentDate
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 세셔널_플랜_생성_시_세션_이름은_공백일_수_없다() {
        String sessionName = "   ";

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> SessionalPlan.create(
                        createReconstructedUser(),
                        sessionName,
                        SESSION_PERIOD,
                        CURRENT_KOREAN_DATE
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 세셔널_플랜_생성_시_세션_이름은_최대_길이를_초과할_수_없다() {
        User user = createReconstructedUser();
        String sessionName = "a".repeat(SESSION_NAME_MAX_LENGTH + 1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> SessionalPlan.create(user, sessionName, SESSION_PERIOD, CURRENT_KOREAN_DATE))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }

    @Test
    void 세셔널_플랜을_수정한다() {
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan();

        sessionalPlan.update(
                "  " + TARGET_SESSION_NAME + "  ",
                TARGET_SESSION_PERIOD,
                CURRENT_KOREAN_DATE
        );

        assertThat(sessionalPlan.getSessionName()).isEqualTo(TARGET_SESSION_NAME);
        assertThat(sessionalPlan.getSessionPeriod()).isEqualTo(TARGET_SESSION_PERIOD);
    }

    @Test
    void 세셔널_플랜_수정_시_세션_시작일이_수정_요청일보다_과거일_수_없다() {
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan();
        SessionPeriod targetSessionPeriod = new SessionPeriod(
                CURRENT_KOREAN_DATE.minusDays(1),
                CURRENT_KOREAN_DATE.plusDays(1)
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> sessionalPlan.update(SESSION_NAME, targetSessionPeriod, CURRENT_KOREAN_DATE))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 세셔널_플랜_수정_시_세션_종료일이_수정_요청일과_같을_수_없다() {
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan();
        LocalDate currentDate = CURRENT_KOREAN_DATE.plusDays(1);
        SessionPeriod targetSessionPeriod = new SessionPeriod(
                currentDate.minusDays(1),
                currentDate
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> sessionalPlan.update(SESSION_NAME, targetSessionPeriod, currentDate))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 세셔널_플랜_수정_시_세션_이름은_공백일_수_없다() {
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan();
        String targetSessionName = "   ";

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> sessionalPlan.update(targetSessionName, SESSION_PERIOD, CURRENT_KOREAN_DATE))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 세셔널_플랜_수정_시_세션_이름은_최대_길이를_초과할_수_없다() {
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan();
        String targetSessionName = "a".repeat(SESSION_NAME_MAX_LENGTH + 1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> sessionalPlan.update(targetSessionName, SESSION_PERIOD, CURRENT_KOREAN_DATE))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }

    @Test
    void 같은_식별자를_가진_세셔널_플랜은_같게_취급한다() {
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID);
        SessionalPlan sameSessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID);

        assertThat(sessionalPlan).isEqualTo(sameSessionalPlan);
        assertThat(sessionalPlan.hashCode()).isEqualTo(sameSessionalPlan.hashCode());
    }

    @Test
    void 식별자가_다른_세셔널_플랜은_다르게_취급한다() {
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID);
        SessionalPlan anotherSessionalPlan = createReconstructedSessionalPlan(ANOTHER_SESSIONAL_PLAN_ID);

        assertThat(sessionalPlan).isNotEqualTo(anotherSessionalPlan);
    }

    @Test
    void 식별자가_없는_세셔널_플랜끼리는_나머지_값이_같더라도_다른_것으로_취급한다() {
        User user = createReconstructedUser();
        SessionalPlan sessionalPlan = SessionalPlan.create(user, SESSION_NAME, SESSION_PERIOD, CURRENT_KOREAN_DATE);
        SessionalPlan anotherSessionalPlan = SessionalPlan.create(
                user,
                SESSION_NAME,
                SESSION_PERIOD,
                CURRENT_KOREAN_DATE
        );

        assertThat(sessionalPlan).isNotEqualTo(anotherSessionalPlan);
    }
}
