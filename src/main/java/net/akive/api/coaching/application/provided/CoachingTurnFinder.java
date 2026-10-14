package net.akive.api.coaching.application.provided;

import net.akive.api.coaching.application.provided.dto.CoachingTurnFindQuery;
import net.akive.api.coaching.application.provided.dto.CoachingTurnResult;

import java.util.Optional;

public interface CoachingTurnFinder {

    Optional<CoachingTurnResult> find(CoachingTurnFindQuery query);
}
