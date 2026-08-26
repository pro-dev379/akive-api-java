package net.akive.api.plan.application.provided.sessionalplan;

import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanCreateCommand;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanDeleteCommand;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanUpdateCommand;

public interface SessionalPlanCreator {

    Long create(SessionalPlanCreateCommand command) throws BadRequestException, ConflictException, NotFoundException;

    void update(SessionalPlanUpdateCommand command) throws BadRequestException, ConflictException, NotFoundException;

    void delete(SessionalPlanDeleteCommand command) throws NotFoundException;
}
