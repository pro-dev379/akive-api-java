package net.akive.api.plan.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.plan.application.dto.command.CreateSessionalPlanCommand;
import net.akive.api.plan.application.dto.command.DeleteSessionalPlanCommand;
import net.akive.api.plan.application.dto.command.UpdateSessionalPlanCommand;
import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;
import net.akive.api.plan.application.port.in.CreateSessionalPlanUseCase;
import net.akive.api.plan.application.port.in.DeleteSessionalPlanUseCase;
import net.akive.api.plan.application.port.in.UpdateSessionalPlanUseCase;
import net.akive.api.plan.application.port.out.*;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.domain.exception.SessionalPlanException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.NOT_FOUND;
import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.SESSION_NAME_ALREADY_EXISTS;

@Service
@Transactional
@RequiredArgsConstructor
class CommandSessionalPlanService implements
        CreateSessionalPlanUseCase,
        DeleteSessionalPlanUseCase,
        UpdateSessionalPlanUseCase {

    private final LoadSessionalPlanPort loadSessionalPlanPort;
    private final ValidateTotalQuantityPort validateTotalQuantityPort;
    private final SaveSessionalPlanPort saveSessionalPlanPort;
    private final DeleteSessionalPlanPort deleteSessionalPlanPort;
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

    @Override
    public void delete(DeleteSessionalPlanCommand command) {
        long deletedCount = deleteSessionalPlanPort.deleteBySessionalPlanIdAndUserId(command.sessionalPlanId(), command.userId());
        if (deletedCount == 0) {
            throw new SessionalPlanException(NOT_FOUND);
        }
    }

    @Override
    public void update(UpdateSessionalPlanCommand command) {
        SessionalPlan sessionalPlan = loadSessionalPlan(command.sessionalPlanId(), command.userId());

        boolean duplicateSessionNameExists =
                hasDuplicateSessionNameExceptCurrent(
                        command.userId(),
                        command.sessionName(),
                        command.sessionalPlanId()
                );

        boolean hasTotalQuantity = !duplicateSessionNameExists && hasTotalQuantity(command.sessionalPlanId());

        SessionalPlan updatedSessionalPlan = sessionalPlan.update(
                command.sessionName(),
                command.sessionStartOn(),
                command.sessionEndOn(),
                duplicateSessionNameExists,
                hasTotalQuantity,
                currentTimeProvider.now()
        );

        saveSessionalPlanPort.save(updatedSessionalPlan);
    }

    private SessionalPlan loadSessionalPlan(Long sessionalPlanId, Long userId) {
        return loadSessionalPlanPort
                .loadBySessionalPlanIdAndUserId(sessionalPlanId, userId)
                .orElseThrow(() -> new SessionalPlanException(NOT_FOUND));
    }

    private boolean hasDuplicateSessionNameExceptCurrent(Long userId, String sessionName, Long sessionalPlanId) {
        return validateSessionalPlanPort.existsByUserIdAndSessionNameAndSessionalPlanIdNot(userId, sessionName, sessionalPlanId);
    }

    private boolean hasTotalQuantity(Long sessionalPlanId) {
        return validateTotalQuantityPort.existsBySessionalPlanId(sessionalPlanId);
    }
}
