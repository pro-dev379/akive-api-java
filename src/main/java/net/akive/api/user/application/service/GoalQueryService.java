package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.provided.GoalFinder;
import net.akive.api.user.application.provided.dto.GoalVersionsFindQuery;
import net.akive.api.user.application.required.GoalRepository;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.GoalVersion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.exception.business.ResourceNotFoundError.GOAL_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GoalQueryService implements GoalFinder {

    private final GoalRepository goalRepository;

    @Override
    public Goal findLatest(Long userId) throws ResourceNotFoundException {
        return goalRepository.findLatestByUserIdAndUserNotWithdrawn(userId).
                orElseThrow(() -> new ResourceNotFoundException(GOAL_NOT_FOUND));
    }

    @Override
    public Goal findLatestBefore(Long userId, LocalDate serviceDate) throws ResourceNotFoundException {
        return goalRepository.findLatestByUserIdAndCreatedAtBefore(userId, serviceDate)
                .orElseThrow(() -> new ResourceNotFoundException(GOAL_NOT_FOUND));
    }

    @Override
    public List<GoalVersion> findVersionsForPeriod(GoalVersionsFindQuery query) {
        return goalRepository.findVersionsForPeriod(query.userId(), query.startOn(), query.endOn());
    }
}
