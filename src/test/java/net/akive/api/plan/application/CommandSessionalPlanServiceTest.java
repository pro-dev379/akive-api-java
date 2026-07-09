package net.akive.api.plan.application;

import net.akive.api.plan.application.dto.command.CreateSessionalPlanCommand;
import net.akive.api.plan.application.dto.command.DeleteSessionalPlanCommand;
import net.akive.api.plan.application.dto.command.UpdateSessionalPlanCommand;
import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;
import net.akive.api.plan.application.port.out.DeleteSessionalPlanPort;
import net.akive.api.plan.application.port.out.LoadSessionalPlanPort;
import net.akive.api.plan.application.port.out.SaveSessionalPlanPort;
import net.akive.api.plan.domain.SessionPeriod;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.domain.exception.SessionalPlanException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.NOT_FOUND;
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
    SaveSessionalPlanPort saveSessionalPlanPort;

    @Mock
    DeleteSessionalPlanPort deleteSessionalPlanPort;

    @Mock
    SessionalPlanDomainService sessionalPlanDomainService;

    @InjectMocks
    CommandSessionalPlanService commandSessionalPlanService;

    @Test
    void 세셔널_플랜을_생성한다() {
        // given
        Long userId = 1L;
        CreateSessionalPlanCommand command = new CreateSessionalPlanCommand(
                userId,
                "1회차",
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );
        SessionalPlan createdSessionalPlan = SessionalPlan.create(
                1L,
                userId,
                "1회차",
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );
        SessionalPlan savedSessionalPlan = SessionalPlan.create(
                1L,
                userId,
                "1회차",
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );

        given(sessionalPlanDomainService.createForUser(
                userId,
                "1회차",
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        )).willReturn(createdSessionalPlan);
        given(saveSessionalPlanPort.save(createdSessionalPlan))
                .willReturn(savedSessionalPlan);

        // when
        SessionalPlanSummaryResult result = commandSessionalPlanService.create(command);

        // then
        assertThat(result.sessionalPlanId()).isEqualTo(1L);
        assertThat(result.sessionName()).isEqualTo("1회차");
        assertThat(result.sessionPeriod()).isEqualTo(
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );

        then(sessionalPlanDomainService).should().createForUser(
                userId,
                "1회차",
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );
        then(saveSessionalPlanPort).should().save(createdSessionalPlan);
    }

    @Test
    void 세셔널_플랜을_생성할_수_없으면_세셔널_플랜을_저장하지_않는다() {
        // given
        Long userId = 1L;
        CreateSessionalPlanCommand command = new CreateSessionalPlanCommand(
                userId,
                "1회차",
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );

        given(sessionalPlanDomainService.createForUser(
                userId,
                "1회차",
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        )).willThrow(new SessionalPlanException(SESSION_NAME_ALREADY_EXISTS));

        // when & then
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> commandSessionalPlanService.create(command))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(SESSION_NAME_ALREADY_EXISTS);

        then(sessionalPlanDomainService).should().createForUser(
                userId,
                "1회차",
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );
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
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );

        given(loadSessionalPlanPort.loadBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.of(sessionalPlan));
        SessionalPlan updatedSessionalPlan = SessionalPlan.create(
                sessionalPlanId,
                userId,
                "수정된 1회차",
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                )
        );
        given(sessionalPlanDomainService.update(
                sessionalPlan,
                "수정된 1회차",
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                )
        )).willReturn(updatedSessionalPlan);

        // when
        commandSessionalPlanService.update(command);

        // then
        then(loadSessionalPlanPort).should().loadBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(sessionalPlanDomainService).should().update(
                sessionalPlan,
                "수정된 1회차",
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                )
        );
        then(saveSessionalPlanPort).should().save(updatedSessionalPlan);
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
        then(sessionalPlanDomainService).should(never()).update(any(), any(), any());
        then(saveSessionalPlanPort).should(never()).save(any(SessionalPlan.class));
    }

    @Test
    void 세셔널_플랜을_수정할_수_없으면_세셔널_플랜을_저장하지_않는다() {
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
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );

        given(loadSessionalPlanPort.loadBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.of(sessionalPlan));
        given(sessionalPlanDomainService.update(
                sessionalPlan,
                "2회차",
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                )
        )).willThrow(new SessionalPlanException(SESSION_NAME_ALREADY_EXISTS));

        // when & then
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> commandSessionalPlanService.update(command))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(SESSION_NAME_ALREADY_EXISTS);

        then(loadSessionalPlanPort).should().loadBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(sessionalPlanDomainService).should().update(
                sessionalPlan,
                "2회차",
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                )
        );
        then(saveSessionalPlanPort).should(never()).save(any(SessionalPlan.class));
    }
}
