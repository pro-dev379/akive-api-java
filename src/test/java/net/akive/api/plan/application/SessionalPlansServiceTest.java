package net.akive.api.plan.application;

import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.plan.application.dto.command.SessionalPlanCreateCommand;
import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;
import net.akive.api.plan.application.port.out.LoadSessionalPlanPort;
import net.akive.api.plan.application.port.out.SaveSessionalPlanPort;
import net.akive.api.plan.application.port.out.ValidateSessionalPlanPort;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.domain.exception.SessionalPlanException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.SESSION_NAME_ALREADY_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SessionalPlansServiceTest {

    @Mock
    LoadSessionalPlanPort loadSessionalPlanPort;

    @Mock
    SaveSessionalPlanPort saveSessionalPlanPort;

    @Mock
    ValidateSessionalPlanPort validateSessionalPlanPort;

    @Mock
    CurrentTimeProvider currentTimeProvider;

    @InjectMocks
    SessionalPlansService sessionalPlansService;

    @Test
    void 회원의_세셔널_플랜_목록을_조회한다() {
        // given
        Long userId = 1L;
        SessionalPlan firstPlan = SessionalPlan.create(
                2L,
                userId,
                "2회차",
                LocalDate.of(2026, 7, 8),
                LocalDate.of(2026, 7, 14)
        );
        SessionalPlan secondPlan = SessionalPlan.create(
                1L,
                userId,
                "1회차",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 7)
        );

        given(loadSessionalPlanPort.loadAllByUserIdOrderBySessionalPlanIdDesc(userId))
                .willReturn(List.of(firstPlan, secondPlan));

        // when
        List<SessionalPlanSummaryResult> results = sessionalPlansService.getAll(userId);

        // then
        assertThat(results)
                .extracting(
                        SessionalPlanSummaryResult::sessionalPlanId,
                        SessionalPlanSummaryResult::sessionName,
                        SessionalPlanSummaryResult::sessionStartOn,
                        SessionalPlanSummaryResult::sessionEndOn
                )
                .containsExactly(
                        tuple(
                                2L,
                                "2회차",
                                LocalDate.of(2026, 7, 8),
                                LocalDate.of(2026, 7, 14)
                        ),
                        tuple(
                                1L,
                                "1회차",
                                LocalDate.of(2026, 7, 1),
                                LocalDate.of(2026, 7, 7)
                        )
                );

        then(loadSessionalPlanPort).should().loadAllByUserIdOrderBySessionalPlanIdDesc(userId);
    }

    @Test
    void 회원의_세셔널_플랜이_없으면_빈_목록을_반환한다() {
        // given
        Long userId = 1L;

        given(loadSessionalPlanPort.loadAllByUserIdOrderBySessionalPlanIdDesc(userId))
                .willReturn(List.of());

        // when
        List<SessionalPlanSummaryResult> results = sessionalPlansService.getAll(userId);

        // then
        assertThat(results).isEmpty();

        then(loadSessionalPlanPort).should().loadAllByUserIdOrderBySessionalPlanIdDesc(userId);
    }

    @Test
    void 세셔널_플랜을_생성한다() {
        // given
        Long userId = 1L;
        SessionalPlanCreateCommand command = SessionalPlanCreateCommand.of(
                userId,
                "1회차",
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );
        SessionalPlan savedSessionalPlan = SessionalPlan.create(
                1L,
                userId,
                "1회차",
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );

        given(validateSessionalPlanPort.existsByUserIdAndSessionName(userId, "1회차"))
                .willReturn(false);
        given(currentTimeProvider.now())
                .willReturn(LocalDateTime.of(2026, 7, 6, 12, 0));
        given(saveSessionalPlanPort.save(any(SessionalPlan.class)))
                .willReturn(savedSessionalPlan);

        // when
        SessionalPlanSummaryResult result = sessionalPlansService.create(command);

        // then
        assertThat(result.sessionalPlanId()).isEqualTo(1L);
        assertThat(result.sessionName()).isEqualTo("1회차");
        assertThat(result.sessionStartOn()).isEqualTo(LocalDate.of(2026, 7, 7));
        assertThat(result.sessionEndOn()).isEqualTo(LocalDate.of(2026, 7, 14));

        then(validateSessionalPlanPort).should().existsByUserIdAndSessionName(userId, "1회차");
        then(saveSessionalPlanPort).should().save(any(SessionalPlan.class));
    }

    @Test
    void 이미_존재하는_세셔널_플랜명이면_세셔널_플랜_생성에_실패한다() {
        // given
        Long userId = 1L;
        SessionalPlanCreateCommand command = SessionalPlanCreateCommand.of(
                userId,
                "1회차",
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );

        given(validateSessionalPlanPort.existsByUserIdAndSessionName(userId, "1회차"))
                .willReturn(true);

        // when & then
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> sessionalPlansService.create(command))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(SESSION_NAME_ALREADY_EXISTS);

        then(validateSessionalPlanPort).should().existsByUserIdAndSessionName(userId, "1회차");
        then(saveSessionalPlanPort).should(never()).save(any(SessionalPlan.class));
    }
}
