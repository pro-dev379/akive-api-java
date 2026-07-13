package net.akive.api.plan.application.service;

import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.common.time.FixedCurrentTimeProvider;
import net.akive.api.plan.domain.SessionPeriod;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.domain.exception.SessionalPlanException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;

import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.SESSION_NAME_ALREADY_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class SessionalPlanDomainServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long SESSIONAL_PLAN_ID = 10L;
    private static final LocalDate REQUESTED_ON = LocalDate.of(2026, 7, 6);
    private static final String REQUESTED_SESSION_NAME = "  1회차  ";
    private static final String REQUESTED_UPDATE_SESSION_NAME = "  수정된 1회차  ";

    @Mock
    SessionalPlanValidator sessionalPlanValidator;

    CurrentTimeProvider currentTimeProvider;

    SessionalPlanDomainService sessionalPlanDomainService;

    @BeforeEach
    void setUp() {
        currentTimeProvider = new FixedCurrentTimeProvider(Instant.parse("2026-07-06T12:00:00Z"));
        sessionalPlanDomainService = new SessionalPlanDomainService(
                sessionalPlanValidator,
                currentTimeProvider
        );
    }

    @Test
    void 생성_가능_검증_후_세셔널_플랜을_생성한다() {
        // given
        SessionPeriod requestedSessionPeriod = sessionPeriod(
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );

        // when
        SessionalPlan createdSessionalPlan = sessionalPlanDomainService.createForUser(
                USER_ID,
                REQUESTED_SESSION_NAME,
                requestedSessionPeriod
        );

        // then
        assertThat(createdSessionalPlan.getUserId()).isEqualTo(USER_ID);
        assertThat(createdSessionalPlan.getSessionName()).isEqualTo("1회차");
        assertThat(createdSessionalPlan.getSessionPeriod()).isEqualTo(requestedSessionPeriod);

        then(sessionalPlanValidator).should().validateCreatable(
                USER_ID,
                REQUESTED_SESSION_NAME,
                REQUESTED_ON
        );
    }

    @Test
    void 생성_가능_검증이_실패하면_세셔널_플랜_생성에_실패한다() {
        // given
        SessionPeriod requestedSessionPeriod = sessionPeriod(
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );
        willThrow(new SessionalPlanException(SESSION_NAME_ALREADY_EXISTS))
                .given(sessionalPlanValidator)
                .validateCreatable(USER_ID, REQUESTED_SESSION_NAME, REQUESTED_ON);

        // when & then
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> sessionalPlanDomainService.createForUser(
                        USER_ID,
                        REQUESTED_SESSION_NAME,
                        requestedSessionPeriod
                ))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(SESSION_NAME_ALREADY_EXISTS);

        then(sessionalPlanValidator).should().validateCreatable(
                USER_ID,
                REQUESTED_SESSION_NAME,
                REQUESTED_ON
        );
    }

    @Test
    void 수정_가능_검증_후_세셔널_플랜을_수정한다() {
        // given
        SessionalPlan currentSessionalPlan = currentSessionalPlan();
        SessionPeriod requestedSessionPeriod = sessionPeriod(
                LocalDate.of(2026, 7, 8),
                LocalDate.of(2026, 7, 15)
        );

        // when
        SessionalPlan updatedSessionalPlan = sessionalPlanDomainService.update(
                currentSessionalPlan,
                REQUESTED_UPDATE_SESSION_NAME,
                requestedSessionPeriod
        );

        // then
        assertThat(updatedSessionalPlan.getSessionalPlanId()).isEqualTo(SESSIONAL_PLAN_ID);
        assertThat(updatedSessionalPlan.getUserId()).isEqualTo(USER_ID);
        assertThat(updatedSessionalPlan.getSessionName()).isEqualTo("수정된 1회차");
        assertThat(updatedSessionalPlan.getSessionPeriod()).isEqualTo(requestedSessionPeriod);

        then(sessionalPlanValidator).should().validateUpdatable(
                currentSessionalPlan,
                REQUESTED_UPDATE_SESSION_NAME,
                requestedSessionPeriod
        );
    }

    @Test
    void 수정_가능_검증이_실패하면_세셔널_플랜_수정에_실패한다() {
        // given
        SessionalPlan currentSessionalPlan = currentSessionalPlan();
        SessionPeriod requestedSessionPeriod = sessionPeriod(
                LocalDate.of(2026, 7, 8),
                LocalDate.of(2026, 7, 15)
        );
        willThrow(new SessionalPlanException(SESSION_NAME_ALREADY_EXISTS))
                .given(sessionalPlanValidator)
                .validateUpdatable(
                        currentSessionalPlan,
                        REQUESTED_UPDATE_SESSION_NAME,
                        requestedSessionPeriod
                );

        // when & then
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> sessionalPlanDomainService.update(
                        currentSessionalPlan,
                        REQUESTED_UPDATE_SESSION_NAME,
                        requestedSessionPeriod
                ))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(SESSION_NAME_ALREADY_EXISTS);

        then(sessionalPlanValidator).should().validateUpdatable(
                currentSessionalPlan,
                REQUESTED_UPDATE_SESSION_NAME,
                requestedSessionPeriod
        );
    }

    private SessionalPlan currentSessionalPlan() {
        return SessionalPlan.create(
                SESSIONAL_PLAN_ID,
                USER_ID,
                "1회차",
                sessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );
    }

    private SessionPeriod sessionPeriod(LocalDate startOn, LocalDate endOn) {
        return new SessionPeriod(startOn, endOn);
    }
}
