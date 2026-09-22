package net.akive.api.dailylog.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.common.pagination.SortCondition;
import net.akive.api.common.pagination.SortDirection;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.dailylog.adapter.persistence.entity.DailyLogEntity;
import net.akive.api.dailylog.application.required.DailyLogRepository;
import net.akive.api.dailylog.domain.ActivityTime;
import net.akive.api.dailylog.domain.DailyLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class DailyLogJpaAdapter implements DailyLogRepository {

    private final DailyLogJpaRepository dailyLogJpaRepository;
    private final CurrentTimeProvider currentTimeProvider;

    @Override
    public DailyLog save(DailyLog dailyLog) {
        LocalDateTime reconstructedAt = currentTimeProvider.now();

        return dailyLogJpaRepository.save(DailyLogEntity.from(dailyLog)).to(reconstructedAt);
    }

    @Override
    public PageResult<LocalDate> findLogOnPageByUserId(Long userId, PageCondition pageCondition) {
        Page<LocalDate> logOnPage = dailyLogJpaRepository.findLogOnPageByUserId(
                userId,
                PageRequest.of(
                        pageCondition.getPage(),
                        pageCondition.getSize(),
                        toSort(pageCondition.getSortCondition())
                )
        );

        return PageResult.of(
                logOnPage.getNumber(),
                logOnPage.getSize(),
                logOnPage.getTotalElements(),
                logOnPage.getTotalPages(),
                logOnPage.isFirst(),
                logOnPage.isLast(),
                logOnPage.getContent()
        );
    }

    @Override
    public Optional<DailyLog> findByDailyLogIdAndUserId(Long dailyLogId, Long userId) {
        LocalDateTime reconstructedAt = currentTimeProvider.now();

        return dailyLogJpaRepository.findByDailyLogIdAndUser_UserId(dailyLogId, userId)
                .map(entity -> entity.to(reconstructedAt));
    }

    @Override
    public List<DailyLog> findAllByUserIdAndLogOnOrderByStartOn(Long userId, LocalDate logOn) {
        LocalDateTime reconstructedAt = currentTimeProvider.now();

        return dailyLogJpaRepository
                .findAllByUserIdAndLogOnOrderByStartOnAsc(userId, logOn)
                .stream()
                .map(entity -> entity.to(reconstructedAt))
                .toList();
    }

    @Override
    public List<DailyLog> findAllByUserIdAndOverlappingTimeRangeOrderByStartOnAsc(
            Long userId,
            LocalDateTime startOn,
            LocalDateTime endOn
    ) {
        LocalDateTime reconstructedAt = currentTimeProvider.now();

        return dailyLogJpaRepository
                .findAllByUserIdAndOverlappingTimeRangeOrderByStartOnAsc(userId, startOn, endOn)
                .stream()
                .map(entity -> entity.to(reconstructedAt))
                .toList();
    }

    @Override
    public boolean existsOverlappingTimeByUserIdAndActivityTime(Long userId, ActivityTime activityTime) {
        return dailyLogJpaRepository.existsOverlappingTimeByUserIdAndActivityTime(
                userId,
                activityTime.getStartOn(),
                activityTime.getEndOn()
        );
    }

    @Override
    public boolean existsOverlappingTimeByUserIdAndActivityTimeAndDailyLogIdNot(
            Long userId,
            ActivityTime activityTime,
            Long dailyLogId
    ) {
        return dailyLogJpaRepository.existsOverlappingTimeByUserIdAndActivityTimeAndDailyLogIdNot(
                userId,
                activityTime.getStartOn(),
                activityTime.getEndOn(),
                dailyLogId
        );
    }

    @Override
    public void delete(DailyLog dailyLog) {
        dailyLogJpaRepository.delete(DailyLogEntity.from(dailyLog));
    }

    private Sort toSort(SortCondition sortCondition) {
        return Sort.by(
                toDirection(sortCondition.sortDirection()),
                toDailyLogProperty(sortCondition.property())
        );
    }

    private Sort.Direction toDirection(SortDirection sortDirection) {
        return sortDirection == SortDirection.ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private String toDailyLogProperty(String property) {
        return switch (property) {
            case "dailyLogId" -> "dailyLogId";
            case "logOn" -> "logOn";
            case "startOn" -> "activityTime.startOn";
            case "endOn" -> "activityTime.endOn";
            default -> throw new IllegalArgumentException("Unsupported daily log sort property: " + property);
        };
    }
}
