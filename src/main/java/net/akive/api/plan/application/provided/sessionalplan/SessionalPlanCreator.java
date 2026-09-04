package net.akive.api.plan.application.provided.sessionalplan;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanCreateCommand;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanDeleteCommand;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanUpdateCommand;

public interface SessionalPlanCreator {

    Long create(SessionalPlanCreateCommand command)
            throws BusinessRuleViolationException, ResourceConflictException, ResourceNotFoundException;

    void update(SessionalPlanUpdateCommand command)
            throws BusinessRuleViolationException, ResourceConflictException, ResourceNotFoundException;

    void delete(SessionalPlanDeleteCommand command) throws ResourceNotFoundException;
}
