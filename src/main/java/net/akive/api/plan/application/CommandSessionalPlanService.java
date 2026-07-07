package net.akive.api.plan.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.plan.application.dto.command.CreateSessionalPlanCommand;
import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;
import net.akive.api.plan.application.port.in.CreateSessionalPlanUseCase;
import net.akive.api.plan.application.port.out.SaveSessionalPlanPort;
import net.akive.api.plan.application.port.out.ValidateSessionalPlanPort;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.domain.exception.SessionalPlanException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.SESSION_NAME_ALREADY_EXISTS;

@Service
@Transactional
@RequiredArgsConstructor
public class CommandSessionalPlanService implements CreateSessionalPlanUseCase {

    private final SaveSessionalPlanPort saveSessionalPlanPort;
    private final ValidateSessionalPlanPort validateSessionalPlanPort;
    private final CurrentTimeProvider currentTimeProvider;

    @Override
    public SessionalPlanSummaryResult create(CreateSessionalPlanCommand command) {
        if (validateSessionalPlanPort.existsByUserIdAndSessionName(command.userId(), command.sessionName())) {
            throw new SessionalPlanException(SESSION_NAME_ALREADY_EXISTS);
        }

        SessionalPlan sessionalPlan = SessionalPlan.createForUser(
                command.userId(),
                command.sessionName(),
                command.sessionStartOn(),
                command.sessionEndOn(),
                currentTimeProvider.now()
        );

        SessionalPlan savedSessionalPlan = saveSessionalPlanPort.save(sessionalPlan);

        return SessionalPlanSummaryResult.from(savedSessionalPlan);
    }
}
