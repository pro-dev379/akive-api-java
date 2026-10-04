package net.akive.api.user.application.provided;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.GoalVersion;
import net.akive.api.user.application.provided.dto.GoalVersionsFindQuery;

import java.time.LocalDate;
import java.util.List;

public interface GoalFinder {

    Goal findLatest(Long userId) throws ResourceNotFoundException;

    Goal findLatestBefore(Long userId, LocalDate serviceDate) throws ResourceNotFoundException;

    List<GoalVersion> findVersionsForPeriod(GoalVersionsFindQuery query);
}
