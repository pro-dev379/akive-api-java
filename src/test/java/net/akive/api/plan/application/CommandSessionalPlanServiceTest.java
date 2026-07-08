package net.akive.api.plan.application;

import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.common.time.FixedCurrentTimeProvider;
import net.akive.api.plan.application.dto.command.CreateSessionalPlanCommand;
import net.akive.api.plan.application.dto.command.DeleteSessionalPlanCommand;
import net.akive.api.plan.application.dto.command.UpdateSessionalPlanCommand;
import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;
import net.akive.api.plan.application.port.out.*;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.domain.exception.SessionalPlanException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.NOT_FOUND;
import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.SESSION_PERIOD_CHANGE_NOT_ALLOWED;
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
    LoadSessionalPlanPort loadSessionalPlanPort;

    @Mock
    ValidateTotalQuantityPort validateTotalQuantityPort;

    @Mock
    SaveSessionalPlanPort saveSessionalPlanPort;

    @Mock
    DeleteSessionalPlanPort deleteSessionalPlanPort;

    @Mock
    ValidateSessionalPlanPort validateSessionalPlanPort;

    CurrentTimeProvider currentTimeProvider;

    CommandSessionalPlanService commandSessionalPlanService;

    @BeforeEach
    void setUp() {
        currentTimeProvider = new FixedCurrentTimeProvider(Instant.parse("2026-07-06T12:00:00Z"));

        commandSessionalPlanService = new CommandSessionalPlanService(
                loadSessionalPlanPort,
                validateTotalQuantityPort,
                saveSessionalPlanPort,
                deleteSessionalPlanPort,
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

    @Test
    void 세셔널_플랜을_삭제한다() {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        DeleteSessionalPlanCommand command = DeleteSessionalPlanCommand.of(userId, sessionalPlanId);

        given(deleteSessionalPlanPort.deleteBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(1L);

        // when
        commandSessionalPlanService.delete(command);

        // then
        then(deleteSessionalPlanPort).should().deleteBySessionalPlanIdAndUserId(sessionalPlanId, userId);
    }

    @Test
    void 삭제할_세셔널_플랜이_없으면_세셔널_플랜_삭제에_실패한다() {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        DeleteSessionalPlanCommand command = DeleteSessionalPlanCommand.of(userId, sessionalPlanId);

        given(deleteSessionalPlanPort.deleteBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(0L);

        // when & then
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> commandSessionalPlanService.delete(command))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(NOT_FOUND);

        then(deleteSessionalPlanPort).should().deleteBySessionalPlanIdAndUserId(sessionalPlanId, userId);
    }

    @Test
    void 세셔널_플랜을_수정한다() {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        UpdateSessionalPlanCommand command = UpdateSessionalPlanCommand.of(
                sessionalPlanId,
                userId,
                "수정된 1회차",
                LocalDate.of(2026, 7, 8),
                LocalDate.of(2026, 7, 15)
        );
        SessionalPlan sessionalPlan = SessionalPlan.create(
                sessionalPlanId,
                userId,
                "1회차",
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );

        given(loadSessionalPlanPort.loadBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.of(sessionalPlan));
        given(validateSessionalPlanPort.existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                userId,
                "수정된 1회차",
                sessionalPlanId
        )).willReturn(false);
        given(validateTotalQuantityPort.existsBySessionalPlanId(sessionalPlanId))
                .willReturn(false);

        // when
        commandSessionalPlanService.update(command);

        // then
        ArgumentCaptor<SessionalPlan> sessionalPlanCaptor = ArgumentCaptor.forClass(SessionalPlan.class);
        then(saveSessionalPlanPort).should().save(sessionalPlanCaptor.capture());

        SessionalPlan updatedSessionalPlan = sessionalPlanCaptor.getValue();
        assertThat(updatedSessionalPlan.getSessionalPlanId()).isEqualTo(sessionalPlanId);
        assertThat(updatedSessionalPlan.getUserId()).isEqualTo(userId);
        assertThat(updatedSessionalPlan.getSessionName()).isEqualTo("수정된 1회차");
        assertThat(updatedSessionalPlan.getSessionStartOn()).isEqualTo(LocalDate.of(2026, 7, 8));
        assertThat(updatedSessionalPlan.getSessionEndOn()).isEqualTo(LocalDate.of(2026, 7, 15));

        then(loadSessionalPlanPort).should().loadBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(validateSessionalPlanPort).should().existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                userId,
                "수정된 1회차",
                sessionalPlanId
        );
        then(validateTotalQuantityPort).should().existsBySessionalPlanId(sessionalPlanId);
    }

    @Test
    void 수정할_세셔널_플랜이_없으면_세셔널_플랜_수정에_실패한다() {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        UpdateSessionalPlanCommand command = UpdateSessionalPlanCommand.of(
                sessionalPlanId,
                userId,
                "수정된 1회차",
                LocalDate.of(2026, 7, 8),
                LocalDate.of(2026, 7, 15)
        );

        given(loadSessionalPlanPort.loadBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> commandSessionalPlanService.update(command))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(NOT_FOUND);

        then(loadSessionalPlanPort).should().loadBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(validateSessionalPlanPort).should(never()).existsByUserIdAndSessionNameAndSessionalPlanIdNot(any(), any(), any());
        then(validateTotalQuantityPort).should(never()).existsBySessionalPlanId(any());
        then(saveSessionalPlanPort).should(never()).save(any(SessionalPlan.class));
    }

    @Test
    void 이미_존재하는_세셔널_플랜명으로_수정하면_세셔널_플랜_수정에_실패한다() {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        UpdateSessionalPlanCommand command = UpdateSessionalPlanCommand.of(
                sessionalPlanId,
                userId,
                "2회차",
                LocalDate.of(2026, 7, 8),
                LocalDate.of(2026, 7, 15)
        );
        SessionalPlan sessionalPlan = SessionalPlan.create(
                sessionalPlanId,
                userId,
                "1회차",
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );

        given(loadSessionalPlanPort.loadBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.of(sessionalPlan));
        given(validateSessionalPlanPort.existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                userId,
                "2회차",
                sessionalPlanId
        )).willReturn(true);

        // when & then
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> commandSessionalPlanService.update(command))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(SESSION_NAME_ALREADY_EXISTS);

        then(loadSessionalPlanPort).should().loadBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(validateSessionalPlanPort).should().existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                userId,
                "2회차",
                sessionalPlanId
        );
        then(validateTotalQuantityPort).should(never()).existsBySessionalPlanId(any());
        then(saveSessionalPlanPort).should(never()).save(any(SessionalPlan.class));
    }

    @Test
    void 세션_총량이_등록된_세셔널_플랜의_기간을_변경하면_세셔널_플랜_수정에_실패한다() {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        UpdateSessionalPlanCommand command = UpdateSessionalPlanCommand.of(
                sessionalPlanId,
                userId,
                "1회차",
                LocalDate.of(2026, 7, 8),
                LocalDate.of(2026, 7, 15)
        );
        SessionalPlan sessionalPlan = SessionalPlan.create(
                sessionalPlanId,
                userId,
                "1회차",
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );

        given(loadSessionalPlanPort.loadBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.of(sessionalPlan));
        given(validateSessionalPlanPort.existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                userId,
                "1회차",
                sessionalPlanId
        )).willReturn(false);
        given(validateTotalQuantityPort.existsBySessionalPlanId(sessionalPlanId))
                .willReturn(true);

        // when & then
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> commandSessionalPlanService.update(command))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(SESSION_PERIOD_CHANGE_NOT_ALLOWED);

        then(loadSessionalPlanPort).should().loadBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(validateSessionalPlanPort).should().existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                userId,
                "1회차",
                sessionalPlanId
        );
        then(validateTotalQuantityPort).should().existsBySessionalPlanId(sessionalPlanId);
        then(saveSessionalPlanPort).should(never()).save(any(SessionalPlan.class));
    }

    @Test
    void 세션_총량이_등록되어도_기간을_변경하지_않으면_세셔널_플랜명은_수정할_수_있다() {
        // given
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        UpdateSessionalPlanCommand command = UpdateSessionalPlanCommand.of(
                sessionalPlanId,
                userId,
                "수정된 1회차",
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );
        SessionalPlan sessionalPlan = SessionalPlan.create(
                sessionalPlanId,
                userId,
                "1회차",
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );

        given(loadSessionalPlanPort.loadBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.of(sessionalPlan));
        given(validateSessionalPlanPort.existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                userId,
                "수정된 1회차",
                sessionalPlanId
        )).willReturn(false);
        given(validateTotalQuantityPort.existsBySessionalPlanId(sessionalPlanId))
                .willReturn(true);

        // when
        commandSessionalPlanService.update(command);

        // then
        ArgumentCaptor<SessionalPlan> sessionalPlanCaptor = ArgumentCaptor.forClass(SessionalPlan.class);
        then(saveSessionalPlanPort).should().save(sessionalPlanCaptor.capture());

        SessionalPlan updatedSessionalPlan = sessionalPlanCaptor.getValue();
        assertThat(updatedSessionalPlan.getSessionalPlanId()).isEqualTo(sessionalPlanId);
        assertThat(updatedSessionalPlan.getUserId()).isEqualTo(userId);
        assertThat(updatedSessionalPlan.getSessionName()).isEqualTo("수정된 1회차");
        assertThat(updatedSessionalPlan.getSessionStartOn()).isEqualTo(LocalDate.of(2026, 7, 7));
        assertThat(updatedSessionalPlan.getSessionEndOn()).isEqualTo(LocalDate.of(2026, 7, 14));

        then(loadSessionalPlanPort).should().loadBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(validateSessionalPlanPort).should().existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                userId,
                "수정된 1회차",
                sessionalPlanId
        );
        then(validateTotalQuantityPort).should().existsBySessionalPlanId(sessionalPlanId);
    }
}
