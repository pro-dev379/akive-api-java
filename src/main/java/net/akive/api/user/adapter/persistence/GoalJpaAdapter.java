package net.akive.api.user.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.user.adapter.persistence.entity.GoalEntity;
import net.akive.api.user.application.required.GoalRepository;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.GoalVersion;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.akive.api.common.time.ServiceDayPolicy.DAILY_BOUNDARY_HOUR;

@Repository
@RequiredArgsConstructor
class GoalJpaAdapter implements GoalRepository {

    private static final ZoneId GOAL_DATE_ZONE = ZoneId.of("Asia/Seoul");
    private final GoalJpaRepository goalJpaRepository;

    @Override
    public Goal save(Goal goal) {
        return goalJpaRepository.save(GoalEntity.from(goal)).to();
    }

    @Override
    public Optional<Goal> findByGoalId(Long goalId) {
        return goalJpaRepository.findById(goalId).map(GoalEntity::to);
    }

    @Override
    public Optional<Goal> findLatestByUserIdAndUserNotWithdrawn(Long userId) {
        return goalJpaRepository.findLatestByUserIdAndUserNotWithdrawn(userId).map(GoalEntity::to);
    }

    @Override
    public List<GoalVersion> findVersionsForPeriod(Long userId, LocalDate startOn, LocalDate endOn) {
        LocalDateTime periodStart = koreanServiceDayStartAsUtc(startOn);
        LocalDateTime periodEnd = koreanServiceDayStartAsUtc(endOn);
        List<GoalVersion> goalVersions = new ArrayList<>();

        goalJpaRepository
                .findLatestByUserIdAndCreatedAtBefore(userId, periodStart)
                .map(this::toVersion)
                .ifPresent(goalVersions::add);

        if (startOn.isBefore(endOn)) {
            goalJpaRepository
                    .findAllByUserIdAndCreatedAtInRange(userId, periodStart, periodEnd)
                    .stream()
                    .map(this::toVersion)
                    .forEach(goalVersions::add);
        }

        return List.copyOf(goalVersions);
    }

    private LocalDateTime koreanServiceDayStartAsUtc(LocalDate date) {
        return date.atTime(DAILY_BOUNDARY_HOUR, 0)
                .atZone(GOAL_DATE_ZONE)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
    }

    private GoalVersion toVersion(GoalEntity entity) {
        return new GoalVersion(
                entity.getRest10MinSec(),
                entity.getMealNum(),
                entity.getMeal1TimeSec(),
                entity.getCreatedAt()
        );
    }
}
