package net.akive.api.plan.application;

import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.common.time.FixedCurrentTimeProvider;
import net.akive.api.plan.application.dto.command.CreateSessionalPlanCommand;
import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;
import net.akive.api.plan.application.port.out.SaveSessionalPlanPort;
import net.akive.api.plan.application.port.out.ValidateSessionalPlanPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CommandSessionalPlanServiceTest {

    @Mock
    ValidateSessionalPlanPort validateSessionalPlanPort;

    @Mock
    SaveSessionalPlanPort saveSessionalPlanPort;

    CurrentTimeProvider currentTimeProvider;

    CommandSessionalPlanService commandSessionalPlanService;

    @BeforeEach
    void setUp() {
        currentTimeProvider = new FixedCurrentTimeProvider(Instant.parse("2026-07-06T12:00:00Z"));

        commandSessionalPlanService = new CommandSessionalPlanService(
                saveSessionalPlanPort,
                validateSessionalPlanPort,
                currentTimeProvider
        );
    }

    @Test
    void 세셔널_플랜을_생성한다() {
        // given
        Long userId = 1L;
        CreateSessionalPlanCommand command = CreateSessionalPlanCommand.of(
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
        given(saveSessionalPlanPort.save(any(SessionalPlan.class)))
                .willReturn(savedSessionalPlan);

        // when
        SessionalPlanSummaryResult result = commandSessionalPlanService.create(command);

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
        CreateSessionalPlanCommand command = CreateSessionalPlanCommand.of(
                userId,
                "1회차",
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );

        given(validateSessionalPlanPort.existsByUserIdAndSessionName(userId, "1회차"))
                .willReturn(true);

        // when & then
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> commandSessionalPlanService.create(command))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(SESSION_NAME_ALREADY_EXISTS);

        then(validateSessionalPlanPort).should().existsByUserIdAndSessionName(userId, "1회차");
        then(saveSessionalPlanPort).should(never()).save(any(SessionalPlan.class));
    }
}