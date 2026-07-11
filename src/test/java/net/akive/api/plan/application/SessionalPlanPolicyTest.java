package net.akive.api.plan.application;

import net.akive.api.plan.application.port.out.ValidateSessionalPlanPort;
import net.akive.api.plan.application.port.out.ValidateTotalQuantityPort;
import net.akive.api.plan.domain.SessionPeriod;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.domain.exception.SessionalPlanException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.SESSION_NAME_ALREADY_EXISTS;
import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.SESSION_PERIOD_CHANGE_NOT_ALLOWED;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SessionalPlanPolicyTest {

    @Mock
    ValidateSessionalPlanPort validateSessionalPlanPort;

    @Mock
    ValidateTotalQuantityPort validateTotalQuantityPort;

    @InjectMocks
    SessionalPlanPolicy sessionalPlanPolicy;

    @Test
    void 중복된_세셔널_플랜명이_아니면_세셔널_플랜을_생성할_수_있다() {
        // given
        given(validateSessionalPlanPort.existsByUserIdAndSessionName(1L, "1회차"))
                .willReturn(false);

        // when & then
        assertThatCode(() -> sessionalPlanPolicy.validateCreatable(1L, "  1회차  "))
                .doesNotThrowAnyException();

        then(validateSessionalPlanPort).should().existsByUserIdAndSessionName(1L, "1회차");
        then(validateTotalQuantityPort).shouldHaveNoInteractions();
    }

    @Test
    void 이미_존재하는_세셔널_플랜명이면_세셔널_플랜을_생성할_수_없다() {
        // given
        given(validateSessionalPlanPort.existsByUserIdAndSessionName(1L, "1회차"))
                .willReturn(true);

        // when & then
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> sessionalPlanPolicy.validateCreatable(1L, "  1회차  "))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(SESSION_NAME_ALREADY_EXISTS);
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
        assertThatCode(() -> sessionalPlanPolicy.validateUpdatable(
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
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> sessionalPlanPolicy.validateUpdatable(
                        currentSessionalPlan,
                        requestedSessionName,
                        requestedSessionPeriod
                ))
                .extracting(SessionalPlanException::getErrorCode)
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
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> sessionalPlanPolicy.validateUpdatable(
                        currentSessionalPlan,
                        "1회차",
                        requestedSessionPeriod
                ))
                .extracting(SessionalPlanException::getErrorCode)
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
        sessionalPlanPolicy.validateUpdatable(
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
