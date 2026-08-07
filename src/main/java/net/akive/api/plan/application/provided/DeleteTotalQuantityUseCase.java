package net.akive.api.plan.application.provided;

import net.akive.api.plan.application.provided.dto.command.DeleteTotalQuantityCommand;

public interface DeleteTotalQuantityUseCase {

    void delete(DeleteTotalQuantityCommand command);
}
