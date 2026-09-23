package net.akive.api.plan.application.service.sessionalplan;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanCreateCommand;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanDeleteCommand;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanUpdateCommand;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.plan.application.fixture.SessionalPlanCommandFixture.*;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.*;
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
        User user = createReconstructedUser(USER_ID);
        SessionalPlan sessionalPlan = createSessionalPlan(user);
        SessionalPlan savedSessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID, sessionalPlan);
        SessionalPlanCreateCommand command = createSessionalPlanCreateCommand();

        given(userFinder.findByUserIdAndNotWithdrawn(command.userId()))
                .willReturn(Optional.of(user));
        given(sessionalPlanDomainService.create(user, command.sessionName(), command.sessionPeriod()))
                .willReturn(sessionalPlan);
        given(sessionalPlanRepository.save(sessionalPlan))
                .willReturn(savedSessionalPlan);

        Long result = sessionalPlanCommandService.create(command);

        assertThat(result).isEqualTo(SESSIONAL_PLAN_ID);

        then(userFinder).should().findByUserIdAndNotWithdrawn(command.userId());
        then(sessionalPlanDomainService).should().create(user, command.sessionName(), command.sessionPeriod());
        then(sessionalPlanRepository).should().save(sessionalPlan);
    }

    @Test
    void 회원이_없으면_세셔널_플랜_생성에_실패하고_예외를_던진다() {
        SessionalPlanCreateCommand command = createSessionalPlanCreateCommand(UNKNOWN_USER_ID);
        given(userFinder.findByUserIdAndNotWithdrawn(command.userId())).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> sessionalPlanCommandService.create(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(userFinder).should().findByUserIdAndNotWithdrawn(command.userId());
        then(sessionalPlanDomainService).should(never()).create(any(), any(), any());
        then(sessionalPlanRepository).should(never()).save(any());
    }

    @Test
    void 세셔널_플랜을_수정한다() {
        SessionalPlan currentSessionalPlan = createReconstructedSessionalPlan();
        SessionalPlanUpdateCommand command =
                createSessionalPlanUpdateCommand(TARGET_SESSION_NAME, TARGET_SESSION_PERIOD);

        given(sessionalPlanRepository.findBySessionalPlanIdAndUserId(command.sessionalPlanId(), command.userId()))
                .willReturn(Optional.of(currentSessionalPlan));

        sessionalPlanCommandService.update(command);

        then(sessionalPlanRepository).should()
                .findBySessionalPlanIdAndUserId(command.sessionalPlanId(), command.userId());
        then(sessionalPlanDomainService).should().update(
                currentSessionalPlan,
                command.targetSessionName(),
                command.targetSessionPeriod()
        );
        then(sessionalPlanRepository).should().save(currentSessionalPlan);
    }

    @Test
    void 세셔널_플랜이_없으면_수정에_실패하고_예외를_던진다() {
        SessionalPlanUpdateCommand command = createSessionalPlanUpdateCommand(
                UNKNOWN_SESSIONAL_PLAN_ID,
                SESSION_NAME,
                SESSION_PERIOD
        );

        given(sessionalPlanRepository.findBySessionalPlanIdAndUserId(command.sessionalPlanId(), command.userId()))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> sessionalPlanCommandService.update(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanRepository).should()
                .findBySessionalPlanIdAndUserId(command.sessionalPlanId(), command.userId());
        then(sessionalPlanDomainService).should(never()).update(any(), any(), any());
        then(sessionalPlanRepository).should(never()).save(any());
    }

    @Test
    void 세셔널_플랜을_삭제한다() {
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID);
        SessionalPlanDeleteCommand command = createSessionalPlanDeleteCommand(SESSIONAL_PLAN_ID);

        given(sessionalPlanRepository.findBySessionalPlanIdAndUserId(command.sessionalPlanId(), command.userId()))
                .willReturn(Optional.of(sessionalPlan));

        sessionalPlanCommandService.delete(command);

        then(sessionalPlanRepository).should()
                .findBySessionalPlanIdAndUserId(command.sessionalPlanId(), command.userId());
        then(sessionalPlanRepository).should().delete(sessionalPlan);
    }

    @Test
    void 세셔널_플랜이_없으면_삭제에_실패하고_예외를_던진다() {
        SessionalPlanDeleteCommand command = createSessionalPlanDeleteCommand(UNKNOWN_SESSIONAL_PLAN_ID);

        given(sessionalPlanRepository.findBySessionalPlanIdAndUserId(command.sessionalPlanId(), command.userId()))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> sessionalPlanCommandService.delete(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanRepository).should()
                .findBySessionalPlanIdAndUserId(command.sessionalPlanId(), command.userId());
        then(sessionalPlanRepository).should(never()).delete(any());
    }
}
