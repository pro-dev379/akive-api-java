package net.akive.api.plan.application.provided;

import net.akive.api.plan.application.provided.dto.result.sessionalplan.GetSessionalPlansResult;

public interface GetSessionalPlansUseCase {

    GetSessionalPlansResult getAll(Long userId);
}
