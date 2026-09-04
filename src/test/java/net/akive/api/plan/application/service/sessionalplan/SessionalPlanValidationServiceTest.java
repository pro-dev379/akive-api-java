package net.akive.api.plan.application.service.sessionalplan;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.value.SessionPeriod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.IN_PROGRESS_SESSIONAL_PLAN_LIMIT_EXCEEDED;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.SESSION_PERIOD_CHANGE_NOT_ALLOWED;
import static net.akive.api.common.exception.business.ResourceConflictError.SESSION_NAME_ALREADY_EXISTS;
import static net.akive.api.plan.domain.PlanFixture.SESSION_NAME;
import static net.akive.api.plan.domain.PlanFixture.createPersistedSessionalPlan;
import static net.akive.api.plan.domain.policy.PlanPolicy.IN_PROGRESS_SESSIONAL_PLAN_MAX_COUNT;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SessionalPlanValidationServiceTest {

    @Mock
    SessionalPlanRepository sessionalPlanRepository;

    @Mock
    TotalQuantityRepository totalQuantityRepository;

    @InjectMocks
    SessionalPlanValidationService sessionalPlanValidationService;

    @Test
    void 세셔널_플랜을_생성할_수_있는지_검증한다() {
        Long userId = 1L;
        LocalDate requestedAt = LocalDate.of(2025, 12, 31);
        String requestedSessionName = "  " + SESSION_NAME + "  ";
        given(sessionalPlanRepository.existsByUserIdAndSessionName(userId, SESSION_NAME))
                .willReturn(false);
        given(sessionalPlanRepository.countInProgressByUserId(userId, requestedAt))
                .willReturn(IN_PROGRESS_SESSIONAL_PLAN_MAX_COUNT - 1);

        sessionalPlanValidationService.validateCreatable(userId, requestedSessionName, requestedAt);

        then(sessionalPlanRepository).should().existsByUserIdAndSessionName(userId, SESSION_NAME);
        then(sessionalPlanRepository).should().countInProgressByUserId(userId, requestedAt);
    }

    @Test
    void 같은_이름의_세셔널_플랜이_있으면_생성할_수_없다() {
        Long userId = 1L;
        LocalDate requestedAt = LocalDate.of(2025, 12, 31);
        given(sessionalPlanRepository.existsByUserIdAndSessionName(userId, SESSION_NAME))
                .willReturn(true);

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> sessionalPlanValidationService.validateCreatable(
                        userId,
                        SESSION_NAME,
                        requestedAt
                ))
                .extracting(ResourceConflictException::getError)
                .isEqualTo(SESSION_NAME_ALREADY_EXISTS);

        then(sessionalPlanRepository).should().existsByUserIdAndSessionName(userId, SESSION_NAME);
        then(sessionalPlanRepository).should(never()).countInProgressByUserId(userId, requestedAt);
    }

    @Test
    void 진행_중인_세셔널_플랜_수가_최대치에_도달하면_생성할_수_없다() {
        Long userId = 1L;
        LocalDate requestedAt = LocalDate.of(2025, 12, 31);
        given(sessionalPlanRepository.existsByUserIdAndSessionName(userId, SESSION_NAME))
                .willReturn(false);
        given(sessionalPlanRepository.countInProgressByUserId(userId, requestedAt))
                .willReturn(IN_PROGRESS_SESSIONAL_PLAN_MAX_COUNT);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> sessionalPlanValidationService.validateCreatable(
                        userId,
                        SESSION_NAME,
                        requestedAt
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(IN_PROGRESS_SESSIONAL_PLAN_LIMIT_EXCEEDED);

        then(sessionalPlanRepository).should().existsByUserIdAndSessionName(userId, SESSION_NAME);
        then(sessionalPlanRepository).should().countInProgressByUserId(userId, requestedAt);
    }

    @Test
    void 세셔널_플랜을_수정할_수_있는지_검증한다() {
        SessionalPlan currentSessionalPlan = createPersistedSessionalPlan();
        SessionPeriod sessionPeriod = currentSessionalPlan.getSessionPeriod();
        given(sessionalPlanRepository.existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                currentSessionalPlan.getUser().getUserId(),
                SESSION_NAME,
                currentSessionalPlan.getSessionalPlanId()
        )).willReturn(false);

        sessionalPlanValidationService.validateUpdatable(currentSessionalPlan, SESSION_NAME, sessionPeriod);

        then(sessionalPlanRepository).should().existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                currentSessionalPlan.getUser().getUserId(),
                SESSION_NAME,
                currentSessionalPlan.getSessionalPlanId()
        );
        then(totalQuantityRepository).should(never())
                .existsBySessionalPlanId(currentSessionalPlan.getSessionalPlanId());
    }

    @Test
    void 세셔널_플랜_기간이_변경되어도_세션_총량이_없으면_수정할_수_있다() {
        SessionalPlan currentSessionalPlan = createPersistedSessionalPlan();
        SessionPeriod newSessionPeriod = createChangedSessionPeriod();
        given(sessionalPlanRepository.existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                currentSessionalPlan.getUser().getUserId(),
                SESSION_NAME,
                currentSessionalPlan.getSessionalPlanId()
        )).willReturn(false);
        given(totalQuantityRepository.existsBySessionalPlanId(currentSessionalPlan.getSessionalPlanId()))
                .willReturn(false);

        sessionalPlanValidationService.validateUpdatable(currentSessionalPlan, SESSION_NAME, newSessionPeriod);

        then(sessionalPlanRepository).should().existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                currentSessionalPlan.getUser().getUserId(),
                SESSION_NAME,
                currentSessionalPlan.getSessionalPlanId()
        );
        then(totalQuantityRepository).should().existsBySessionalPlanId(currentSessionalPlan.getSessionalPlanId());
    }

    @Test
    void 같은_이름의_다른_세셔널_플랜이_있으면_수정할_수_없다() {
        SessionalPlan currentSessionalPlan = createPersistedSessionalPlan();
        SessionPeriod sessionPeriod = currentSessionalPlan.getSessionPeriod();
        given(sessionalPlanRepository.existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                currentSessionalPlan.getUser().getUserId(),
                SESSION_NAME,
                currentSessionalPlan.getSessionalPlanId()
        )).willReturn(true);

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> sessionalPlanValidationService.validateUpdatable(
                        currentSessionalPlan,
                        SESSION_NAME,
                        sessionPeriod
                ))
                .extracting(ResourceConflictException::getError)
                .isEqualTo(SESSION_NAME_ALREADY_EXISTS);

        then(sessionalPlanRepository).should().existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                currentSessionalPlan.getUser().getUserId(),
                SESSION_NAME,
                currentSessionalPlan.getSessionalPlanId()
        );
        then(totalQuantityRepository).should(never())
                .existsBySessionalPlanId(currentSessionalPlan.getSessionalPlanId());
    }

    @Test
    void 세션_총량이_있으면_세셔널_플랜_기간을_변경할_수_없다() {
        SessionalPlan currentSessionalPlan = createPersistedSessionalPlan();
        SessionPeriod newSessionPeriod = createChangedSessionPeriod();
        given(sessionalPlanRepository.existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                currentSessionalPlan.getUser().getUserId(),
                SESSION_NAME,
                currentSessionalPlan.getSessionalPlanId()
        )).willReturn(false);
        given(totalQuantityRepository.existsBySessionalPlanId(currentSessionalPlan.getSessionalPlanId()))
                .willReturn(true);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> sessionalPlanValidationService.validateUpdatable(
                        currentSessionalPlan,
                        SESSION_NAME,
                        newSessionPeriod
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(SESSION_PERIOD_CHANGE_NOT_ALLOWED);

        then(sessionalPlanRepository).should().existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                currentSessionalPlan.getUser().getUserId(),
                SESSION_NAME,
                currentSessionalPlan.getSessionalPlanId()
        );
        then(totalQuantityRepository).should().existsBySessionalPlanId(currentSessionalPlan.getSessionalPlanId());
    }

    private SessionPeriod createChangedSessionPeriod() {
        return new SessionPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 13)
        );
    }
}
