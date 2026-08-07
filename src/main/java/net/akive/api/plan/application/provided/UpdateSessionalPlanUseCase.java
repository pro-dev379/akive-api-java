package net.akive.api.plan.application.provided;

import net.akive.api.plan.application.provided.dto.command.UpdateSessionalPlanCommand;

public interface UpdateSessionalPlanUseCase {

    void update(UpdateSessionalPlanCommand command);
}
