package net.akive.api.plan.application.service;

import net.akive.api.plan.application.port.out.ValidateSessionalPlanPort;
import net.akive.api.plan.application.port.out.ValidateTotalQuantityPort;
import net.akive.api.plan.domain.SessionPeriod;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.domain.exception.PlanException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static net.akive.api.plan.domain.exception.PlanErrorCode.*;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SessionalPlanValidatorTest {

    private static final Long USER_ID = 1L;
    private static final LocalDate REQUESTED_ON = LocalDate.of(2026, 7, 12);

    @Mock
    ValidateSessionalPlanPort validateSessionalPlanPort;

    @Mock
    ValidateTotalQuantityPort validateTotalQuantityPort;

    @InjectMocks
    SessionalPlanValidator sessionalPlanValidator;

    @Test
    void 중복된_세셔널_플랜명이_아니고_진행_중인_세셔널_플랜이_5개_미만이면_생성할_수_있다() {
        // given
        given(validateSessionalPlanPort.existsByUserIdAndSessionName(USER_ID, "1회차"))
                .willReturn(false);
        given(validateSessionalPlanPort.countInProgressByUserId(USER_ID, REQUESTED_ON))
                .willReturn(4);

        // when & then
        assertThatCode(() -> sessionalPlanValidator.validateCreatable(USER_ID, "  1회차  ", REQUESTED_ON))
                .doesNotThrowAnyException();

        then(validateSessionalPlanPort).should().existsByUserIdAndSessionName(USER_ID, "1회차");
        then(validateSessionalPlanPort).should().countInProgressByUserId(USER_ID, REQUESTED_ON);
        then(validateTotalQuantityPort).shouldHaveNoInteractions();
    }

    @Test
    void 이미_존재하는_세셔널_플랜명이면_세셔널_플랜을_생성할_수_없다() {
        // given
        given(validateSessionalPlanPort.existsByUserIdAndSessionName(USER_ID, "1회차"))
                .willReturn(true);

        // when & then
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> sessionalPlanValidator.validateCreatable(USER_ID, "  1회차  ", REQUESTED_ON))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(SESSION_NAME_ALREADY_EXISTS);

        then(validateSessionalPlanPort).should(never()).countInProgressByUserId(any(), any());
        then(validateTotalQuantityPort).shouldHaveNoInteractions();
    }

    @Test
    void 현재_진행_중인_세셔널_플랜_개수가_5개_이상이면_추가로_생성할_수_없다() {
        // given
        given(validateSessionalPlanPort.existsByUserIdAndSessionName(USER_ID, "1회차"))
                .willReturn(false);
        given(validateSessionalPlanPort.countInProgressByUserId(USER_ID, REQUESTED_ON))
                .willReturn(5);

        // when & then
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> sessionalPlanValidator.validateCreatable(USER_ID, "1회차", REQUESTED_ON))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(IN_PROGRESS_SESSIONAL_PLAN_LIMIT_EXCEEDED);

        then(validateSessionalPlanPort).should().existsByUserIdAndSessionName(USER_ID, "1회차");
        then(validateSessionalPlanPort).should().countInProgressByUserId(USER_ID, REQUESTED_ON);
        then(validateTotalQuantityPort).shouldHaveNoInteractions();
    }

    @Test
    void 중복된_세셔널_플랜명이_아니고_세션_총량이_없으면_기간을_변경할_수_있다() {
        // given
        SessionalPlan currentSessionalPlan = currentSessionalPlan();
        SessionPeriod requestedSessionPeriod =
                new SessionPeriod(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                );
        String requestedSessionName = "  수정된 1회차  ";
        given(validateSessionalPlanPort.existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                1L,
                "수정된 1회차",
                10L
        )).willReturn(false);
        given(validateTotalQuantityPort.existsBySessionalPlanId(10L))
                .willReturn(false);

        // when & then
        assertThatCode(() -> sessionalPlanValidator.validateUpdatable(
                currentSessionalPlan,
                requestedSessionName,
                requestedSessionPeriod
        )).doesNotThrowAnyException();

        then(validateSessionalPlanPort).should().existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                1L,
                "수정된 1회차",
                10L
        );
        then(validateTotalQuantityPort).should().existsBySessionalPlanId(10L);
    }

    @Test
    void 이미_존재하는_세셔널_플랜명으로는_수정할_수_없다() {
        // given
        SessionalPlan currentSessionalPlan = currentSessionalPlan();
        SessionPeriod requestedSessionPeriod =
                new SessionPeriod(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                );
        String requestedSessionName = "  2회차  ";
        given(validateSessionalPlanPort.existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                1L,
                "2회차",
                10L
        )).willReturn(true);

        // when & then
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> sessionalPlanValidator.validateUpdatable(
                        currentSessionalPlan,
                        requestedSessionName,
                        requestedSessionPeriod
                ))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(SESSION_NAME_ALREADY_EXISTS);

        then(validateTotalQuantityPort).should(never()).existsBySessionalPlanId(any());
    }

    @Test
    void 세션_총량이_등록된_세셔널_플랜의_기간은_변경할_수_없다() {
        // given
        SessionalPlan currentSessionalPlan = currentSessionalPlan();
        SessionPeriod requestedSessionPeriod =
                new SessionPeriod(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                );
        given(validateSessionalPlanPort.existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                1L,
                "1회차",
                10L
        )).willReturn(false);
        given(validateTotalQuantityPort.existsBySessionalPlanId(10L))
                .willReturn(true);

        // when & then
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> sessionalPlanValidator.validateUpdatable(
                        currentSessionalPlan,
                        "1회차",
                        requestedSessionPeriod
                ))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(SESSION_PERIOD_CHANGE_NOT_ALLOWED);
    }

    @Test
    void 기간을_변경하지_않으면_세션_총량_존재_여부를_조회하지_않는다() {
        // given
        SessionalPlan currentSessionalPlan = currentSessionalPlan();
        SessionPeriod requestedSessionPeriod =
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                );
        given(validateSessionalPlanPort.existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                1L,
                "수정된 1회차",
                10L
        )).willReturn(false);

        // when
        sessionalPlanValidator.validateUpdatable(
                currentSessionalPlan,
                "수정된 1회차",
                requestedSessionPeriod
        );

        // then
        then(validateTotalQuantityPort).should(never()).existsBySessionalPlanId(any());
    }

    private SessionalPlan currentSessionalPlan() {
        return SessionalPlan.create(
                10L,
                1L,
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );
    }
}
