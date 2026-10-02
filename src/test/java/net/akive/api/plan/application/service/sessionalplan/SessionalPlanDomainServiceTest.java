package net.akive.api.plan.application.service.sessionalplan;

import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static net.akive.api.common.exception.business.ResourceConflictError.SESSION_NAME_ALREADY_EXISTS;
import static net.akive.api.common.time.ServiceDayPolicy.toInstantAtServiceZone;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;
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
        User user = createReconstructedUser();
        given(currentTimeProvider.now()).willReturn(CURRENT_INSTANT);

        SessionalPlan sessionalPlan = sessionalPlanDomainService.create(user, SESSION_NAME, SESSION_PERIOD);

        assertThat(sessionalPlan.getUser()).isEqualTo(user);
        assertThat(sessionalPlan.getSessionName()).isEqualTo(SESSION_NAME);
        assertThat(sessionalPlan.getSessionPeriod()).isEqualTo(SESSION_PERIOD);

        then(currentTimeProvider).should().now();
        then(sessionalPlanValidationService).should()
                .validateCreatable(user.getUserId(), SESSION_NAME, CURRENT_KOREAN_DATE);
    }

    @Test
    void 세셔널_플랜_생성_검증에_실패하면_예외를_전파하고_생성하지_않는다() {
        User user = createReconstructedUser();
        ResourceConflictException exception = new ResourceConflictException(SESSION_NAME_ALREADY_EXISTS);
        given(currentTimeProvider.now()).willReturn(CURRENT_INSTANT);
        willThrow(exception)
                .given(sessionalPlanValidationService)
                .validateCreatable(user.getUserId(), SESSION_NAME, CURRENT_KOREAN_DATE);

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> sessionalPlanDomainService.create(user, SESSION_NAME, SESSION_PERIOD))
                .isSameAs(exception);

        then(currentTimeProvider).should().now();
        then(sessionalPlanValidationService).should()
                .validateCreatable(user.getUserId(), SESSION_NAME, CURRENT_KOREAN_DATE);
    }

    @Test
    void 세셔널_플랜을_수정한다() {
        SessionalPlan currentSessionalPlan = createReconstructedSessionalPlan();
        Instant currentInstant = toInstantAtServiceZone(CURRENT_KOREAN_DATE.plusDays(1).atTime(10, 0));
        given(currentTimeProvider.now()).willReturn(currentInstant);

        sessionalPlanDomainService.update(currentSessionalPlan, TARGET_SESSION_NAME, TARGET_SESSION_PERIOD);

        assertThat(currentSessionalPlan.getSessionName()).isEqualTo(TARGET_SESSION_NAME);
        assertThat(currentSessionalPlan.getSessionPeriod()).isEqualTo(TARGET_SESSION_PERIOD);

        then(sessionalPlanValidationService).should()
                .validateUpdatable(currentSessionalPlan, TARGET_SESSION_NAME, TARGET_SESSION_PERIOD);
        then(currentTimeProvider).should().now();
    }

    @Test
    void 세셔널_플랜_수정_검증에_실패하면_예외를_전파하고_수정하지_않는다() {
        SessionalPlan currentSessionalPlan = createReconstructedSessionalPlan();
        ResourceConflictException exception = new ResourceConflictException(SESSION_NAME_ALREADY_EXISTS);
        willThrow(exception)
                .given(sessionalPlanValidationService)
                .validateUpdatable(currentSessionalPlan, TARGET_SESSION_NAME, TARGET_SESSION_PERIOD);

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> sessionalPlanDomainService.update(
                        currentSessionalPlan,
                        TARGET_SESSION_NAME,
                        TARGET_SESSION_PERIOD
                ))
                .isSameAs(exception);

        assertThat(currentSessionalPlan.getSessionName()).isEqualTo(SESSION_NAME);
        assertThat(currentSessionalPlan.getSessionPeriod()).isEqualTo(SESSION_PERIOD);
        then(sessionalPlanValidationService).should()
                .validateUpdatable(currentSessionalPlan, TARGET_SESSION_NAME, TARGET_SESSION_PERIOD);
        then(currentTimeProvider).should(never()).now();
    }
}
