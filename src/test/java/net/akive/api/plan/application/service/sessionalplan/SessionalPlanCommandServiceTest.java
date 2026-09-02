package net.akive.api.plan.application.service.sessionalplan;

import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanCreateCommand;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanDeleteCommand;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanUpdateCommand;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.value.SessionPeriod;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.NotFoundError.USER_NOT_FOUND;
import static net.akive.api.plan.domain.PlanFixture.*;
import static net.akive.api.user.domain.UserFixture.createPersistedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SessionalPlanCommandServiceTest {

    @Mock
    UserFinder userFinder;

    @Mock
    SessionalPlanRepository sessionalPlanRepository;

    @Mock
    SessionalPlanDomainService sessionalPlanDomainService;

    @InjectMocks
    SessionalPlanCommandService sessionalPlanCommandService;

    @Test
    void 세셔널_플랜을_생성한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        User user = createPersistedUser(userId);
        SessionPeriod sessionPeriod = createSessionPeriod();
        SessionalPlan sessionalPlan = createSessionalPlan(user);
        SessionalPlan savedSessionalPlan = SessionalPlan.reconstruct(
                sessionalPlanId,
                user,
                SESSION_NAME,
                sessionPeriod
        );
        SessionalPlanCreateCommand command = new SessionalPlanCreateCommand(
                userId,
                SESSION_NAME,
                sessionPeriod
        );

        given(userFinder.findByUserIdAndLeaveFalse(userId))
                .willReturn(Optional.of(user));
        given(sessionalPlanDomainService.create(user, SESSION_NAME, sessionPeriod))
                .willReturn(sessionalPlan);
        given(sessionalPlanRepository.save(sessionalPlan))
                .willReturn(savedSessionalPlan);

        Long result = sessionalPlanCommandService.create(command);

        assertThat(result).isEqualTo(sessionalPlanId);

        then(userFinder).should().findByUserIdAndLeaveFalse(userId);
        then(sessionalPlanDomainService).should().create(user, SESSION_NAME, sessionPeriod);
        then(sessionalPlanRepository).should().save(sessionalPlan);
    }

    @Test
    void 회원이_없으면_세셔널_플랜_생성에_실패하고_예외를_던진다() {
        Long userId = 1L;
        SessionPeriod sessionPeriod = createSessionPeriod();
        SessionalPlanCreateCommand command = new SessionalPlanCreateCommand(
                userId,
                SESSION_NAME,
                sessionPeriod
        );

        given(userFinder.findByUserIdAndLeaveFalse(userId)).willReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> sessionalPlanCommandService.create(command))
                .extracting(NotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(userFinder).should().findByUserIdAndLeaveFalse(userId);
        then(sessionalPlanDomainService).should(never()).create(any(), any(), any());
        then(sessionalPlanRepository).should(never()).save(any());
    }

    @Test
    void 세셔널_플랜을_수정한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        String targetSessionName = "2026 2월 학습";
        User user = createPersistedUser(userId);
        SessionPeriod sessionPeriod = createSessionPeriod();
        SessionalPlan currentSessionalPlan = SessionalPlan.reconstruct(
                sessionalPlanId,
                user,
                SESSION_NAME,
                sessionPeriod
        );
        SessionalPlan updatedSessionalPlan = SessionalPlan.reconstruct(
                sessionalPlanId,
                user,
                targetSessionName,
                sessionPeriod
        );
        SessionalPlanUpdateCommand command = new SessionalPlanUpdateCommand(
                sessionalPlanId,
                userId,
                targetSessionName,
                sessionPeriod
        );

        given(sessionalPlanRepository.findBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.of(currentSessionalPlan));

        sessionalPlanCommandService.update(command);

        then(sessionalPlanRepository).should().findBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(sessionalPlanDomainService).should().update(currentSessionalPlan, targetSessionName, sessionPeriod);
        then(sessionalPlanRepository).should().save(updatedSessionalPlan);
    }

    @Test
    void 세셔널_플랜이_없으면_수정에_실패하고_예외를_던진다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        SessionPeriod sessionPeriod = createSessionPeriod();
        SessionalPlanUpdateCommand command = new SessionalPlanUpdateCommand(
                sessionalPlanId,
                userId,
                SESSION_NAME,
                sessionPeriod
        );

        given(sessionalPlanRepository.findBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> sessionalPlanCommandService.update(command))
                .extracting(NotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanRepository).should().findBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(sessionalPlanDomainService).should(never()).update(any(), any(), any());
        then(sessionalPlanRepository).should(never()).save(any());
    }

    @Test
    void 세셔널_플랜을_삭제한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        SessionalPlanDeleteCommand command = new SessionalPlanDeleteCommand(userId, sessionalPlanId);

        sessionalPlanCommandService.delete(command);

        then(sessionalPlanRepository).should().deleteBySessionalPlanIdAndUserId(sessionalPlanId, userId);
    }
}
