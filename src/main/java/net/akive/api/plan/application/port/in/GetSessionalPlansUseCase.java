package net.akive.api.plan.application.port.in;

import net.akive.api.plan.application.dto.result.sessionalplan.GetSessionalPlansResult;

public interface GetSessionalPlansUseCase {

    GetSessionalPlansResult getAll(Long userId);
}
