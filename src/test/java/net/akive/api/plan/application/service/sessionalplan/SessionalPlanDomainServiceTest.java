package net.akive.api.plan.application.service.sessionalplan;

import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.value.SessionPeriod;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static net.akive.api.common.exception.business.ConflictError.SESSION_NAME_ALREADY_EXISTS;
import static net.akive.api.plan.domain.PlanFixture.*;
import static net.akive.api.user.domain.UserFixture.createPersistedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SessionalPlanDomainServiceTest {

    @Mock
    SessionalPlanValidationService sessionalPlanValidationService;

    @Mock
    CurrentTimeProvider currentTimeProvider;

    @InjectMocks
    SessionalPlanDomainService sessionalPlanDomainService;

    @Test
    void 세셔널_플랜을_생성한다() {
        User user = createPersistedUser(1L);
        SessionPeriod sessionPeriod = createSessionPeriod();
        given(currentTimeProvider.now()).willReturn(REQUESTED_AT);

        SessionalPlan sessionalPlan = sessionalPlanDomainService.create(user, SESSION_NAME, sessionPeriod);

        assertThat(sessionalPlan.getUser()).isEqualTo(user);
        assertThat(sessionalPlan.getSessionName()).isEqualTo(SESSION_NAME);
        assertThat(sessionalPlan.getSessionPeriod()).isEqualTo(sessionPeriod);

        then(currentTimeProvider).should().now();
        then(sessionalPlanValidationService).should()
                .validateCreatable(user.getUserId(), SESSION_NAME, REQUESTED_AT.toLocalDate());
    }

    @Test
    void 세셔널_플랜_생성_검증에_실패하면_예외를_던진다() {
        User user = createPersistedUser(1L);
        SessionPeriod sessionPeriod = createSessionPeriod();
        ConflictException exception = new ConflictException(SESSION_NAME_ALREADY_EXISTS);
        given(currentTimeProvider.now()).willReturn(REQUESTED_AT);
        willThrow(exception)
                .given(sessionalPlanValidationService)
                .validateCreatable(user.getUserId(), SESSION_NAME, REQUESTED_AT.toLocalDate());

        assertThatExceptionOfType(ConflictException.class)
                .isThrownBy(() -> sessionalPlanDomainService.create(user, SESSION_NAME, sessionPeriod))
                .isSameAs(exception);

        then(currentTimeProvider).should().now();
        then(sessionalPlanValidationService).should()
                .validateCreatable(user.getUserId(), SESSION_NAME, REQUESTED_AT.toLocalDate());
    }

    @Test
    void 세셔널_플랜을_수정한다() {
        String targetSessionName = "2026 2월 학습";
        User user = createPersistedUser(1L);
        SessionalPlan currentSessionalPlan = createSessionalPlan(user);
        SessionPeriod sessionPeriod = createSessionPeriod();
        LocalDateTime requestedAt = REQUESTED_AT.plusDays(1);
        given(currentTimeProvider.now()).willReturn(requestedAt);

        sessionalPlanDomainService.update(currentSessionalPlan, targetSessionName, sessionPeriod);

        assertThat(currentSessionalPlan.getSessionName()).isEqualTo(targetSessionName);
        assertThat(currentSessionalPlan.getSessionPeriod()).isEqualTo(sessionPeriod);

        then(sessionalPlanValidationService).should()
                .validateUpdatable(currentSessionalPlan, targetSessionName, sessionPeriod);
        then(currentTimeProvider).should().now();
    }

    @Test
    void 세셔널_플랜_수정_검증에_실패하면_예외를_던진다() {
        String targetSessionName = "2026 2월 학습";
        User user = createPersistedUser(1L);
        SessionalPlan currentSessionalPlan = createSessionalPlan(user);
        SessionPeriod sessionPeriod = createSessionPeriod();
        ConflictException exception = new ConflictException(SESSION_NAME_ALREADY_EXISTS);
        willThrow(exception)
                .given(sessionalPlanValidationService)
                .validateUpdatable(currentSessionalPlan, targetSessionName, sessionPeriod);

        assertThatExceptionOfType(ConflictException.class)
                .isThrownBy(() -> sessionalPlanDomainService.update(
                        currentSessionalPlan,
                        targetSessionName,
                        sessionPeriod
                ))
                .isSameAs(exception);

        then(sessionalPlanValidationService).should()
                .validateUpdatable(currentSessionalPlan, targetSessionName, sessionPeriod);
        then(currentTimeProvider).should(never()).now();
    }
}
