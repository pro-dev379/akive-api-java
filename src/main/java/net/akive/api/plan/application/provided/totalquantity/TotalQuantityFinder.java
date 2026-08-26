package net.akive.api.plan.application.provided.totalquantity;

import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.totalquantity.dto.SessionalPlanTotalQuantityFindQuery;
import net.akive.api.plan.application.result.TotalQuantityProgress;

public interface TotalQuantityFinder {

    TotalQuantityProgress find(SessionalPlanTotalQuantityFindQuery query) throws NotFoundException;
}
