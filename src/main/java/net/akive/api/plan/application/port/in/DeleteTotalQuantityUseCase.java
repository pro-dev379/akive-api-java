package net.akive.api.plan.application.port.in;

import net.akive.api.plan.application.dto.command.DeleteTotalQuantityCommand;

public interface DeleteTotalQuantityUseCase {

    void delete(DeleteTotalQuantityCommand command);
}
