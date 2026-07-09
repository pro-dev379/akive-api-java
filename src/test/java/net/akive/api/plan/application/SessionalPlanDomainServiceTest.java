package net.akive.api.plan.application;

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

    @Mock
    SessionalPlanPolicy sessionalPlanPolicy;

    CurrentTimeProvider currentTimeProvider;

    SessionalPlanDomainService sessionalPlanDomainService;

    @BeforeEach
    void setUp() {
        currentTimeProvider = new FixedCurrentTimeProvider(Instant.parse("2026-07-06T12:00:00Z"));
        sessionalPlanDomainService = new SessionalPlanDomainService(
                sessionalPlanPolicy,
                currentTimeProvider
        );
    }

    @Test
    void 세셔널_플랜명_정규화와_생성_가능_검증_후_세셔널_플랜을_생성한다() {
        // when
        SessionalPlan createdSessionalPlan = sessionalPlanDomainService.createForUser(
                1L,
                "  1회차  ",
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );

        // then
        assertThat(createdSessionalPlan.getUserId()).isEqualTo(1L);
        assertThat(createdSessionalPlan.getSessionName()).isEqualTo("1회차");
        assertThat(createdSessionalPlan.getSessionPeriod()).isEqualTo(
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );

        then(sessionalPlanPolicy).should().validateCreatable(1L, "  1회차  ");
    }

    @Test
    void 생성_가능_검증이_실패하면_세셔널_플랜_생성에_실패한다() {
        // given
        willThrow(new SessionalPlanException(SESSION_NAME_ALREADY_EXISTS))
                .given(sessionalPlanPolicy)
                .validateCreatable(1L, "  1회차  ");

        // when & then
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> sessionalPlanDomainService.createForUser(
                        1L,
                        "  1회차  ",
                        SessionPeriod.of(
                                LocalDate.of(2026, 7, 7),
                                LocalDate.of(2026, 7, 14)
                        )
                ))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(SESSION_NAME_ALREADY_EXISTS);

        then(sessionalPlanPolicy).should().validateCreatable(1L, "  1회차  ");
    }

    @Test
    void 세셔널_플랜명_정규화와_수정_가능_검증_후_세셔널_플랜을_수정한다() {
        // given
        SessionalPlan createdSessionalPlan = SessionalPlan.create(
                10L,
                1L,
                "1회차",
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );
        SessionPeriod requestedSessionPeriod = SessionPeriod.of(
                LocalDate.of(2026, 7, 8),
                LocalDate.of(2026, 7, 15)
        );

        // when
        SessionalPlan updatedSessionalPlan = sessionalPlanDomainService.update(
                createdSessionalPlan,
                "  수정된 1회차  ",
                requestedSessionPeriod
        );

        // then
        assertThat(updatedSessionalPlan.getSessionalPlanId()).isEqualTo(10L);
        assertThat(updatedSessionalPlan.getUserId()).isEqualTo(1L);
        assertThat(updatedSessionalPlan.getSessionName()).isEqualTo("수정된 1회차");
        assertThat(updatedSessionalPlan.getSessionPeriod()).isEqualTo(
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                )
        );

        then(sessionalPlanPolicy).should().validateUpdatable(
                createdSessionalPlan,
                "  수정된 1회차  ",
                requestedSessionPeriod
        );
    }

    @Test
    void 수정_가능_검증이_실패하면_세셔널_플랜_수정에_실패한다() {
        // given
        SessionalPlan createdSessionalPlan = SessionalPlan.create(
                10L,
                1L,
                "1회차",
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );
        willThrow(new SessionalPlanException(SESSION_NAME_ALREADY_EXISTS))
                .given(sessionalPlanPolicy)
                .validateUpdatable(
                        createdSessionalPlan,
                        "  수정된 1회차  ",
                        SessionPeriod.of(
                                LocalDate.of(2026, 7, 8),
                                LocalDate.of(2026, 7, 15)
                        )
                );

        // when & then
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> sessionalPlanDomainService.update(
                        createdSessionalPlan,
                        "  수정된 1회차  ",
                        SessionPeriod.of(
                                LocalDate.of(2026, 7, 8),
                                LocalDate.of(2026, 7, 15)
                        )
                ))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(SESSION_NAME_ALREADY_EXISTS);

        then(sessionalPlanPolicy).should().validateUpdatable(
                createdSessionalPlan,
                "  수정된 1회차  ",
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                )
        );
    }
}
