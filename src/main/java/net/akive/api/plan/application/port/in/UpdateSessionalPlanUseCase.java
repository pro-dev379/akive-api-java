package net.akive.api.plan.application.port.in;

import net.akive.api.plan.application.dto.command.UpdateSessionalPlanCommand;

public interface UpdateSessionalPlanUseCase {

    void update(UpdateSessionalPlanCommand command);
}
