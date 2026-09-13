package net.akive.api.dailylog.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.common.pagination.SortCondition;
import net.akive.api.common.pagination.SortDirection;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.dailylog.adapter.persistence.entity.DailyLogEntity;
import net.akive.api.dailylog.application.required.DailyLogRepository;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.dailylog.domain.DailyLogTimeRange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
class DailyLogJpaAdapter implements DailyLogRepository {

    private static final Set<String> DAILY_LOG_SORT_PROPERTIES = Set.of("logOn");

    private final DailyLogJpaRepository dailyLogJpaRepository;
    private final CurrentTimeProvider currentTimeProvider;

    @Override
    public DailyLog save(DailyLog dailyLog) {
        return dailyLogJpaRepository.save(DailyLogEntity.from(dailyLog)).to(currentTimeProvider.now());
    }

    @Override
    public List<DailyLog> findAllByUserIdAndLogOnOrderByStartOn(Long userId, LocalDate logOn) {
        return dailyLogJpaRepository
                .findAllByUserIdAndLogOnOrderByStartOn(userId, logOn)
                .stream()
                .map(entity -> entity.to(currentTimeProvider.now()))
                .toList();
    }

    @Override
    public PageResult<LocalDate> findLogOnsByUserId(Long userId, PageCondition pageCondition) {
        Page<LocalDate> logOns = dailyLogJpaRepository.findLogOnsByUserId(
                userId,
                PageRequest.of(
                        pageCondition.getPage(),
                        pageCondition.getSize(),
                        toSort(pageCondition.getSortCondition())
                )
        );

        return new PageResult<>(
                logOns.getNumber(),
                logOns.getSize(),
                logOns.getTotalElements(),
                logOns.getTotalPages(),
                logOns.isFirst(),
                logOns.isLast(),
                logOns.getContent()
        );
    }

    @Override
    public boolean existsOverlappingTimeByUserIdAndTimeRange(Long userId, DailyLogTimeRange timeRange) {
        return dailyLogJpaRepository.existsOverlappingTimeByUserIdAndTimeRange(
                userId,
                timeRange.getStartOn(),
                timeRange.getEndOn()
        );
    }

    @Override
    public boolean existsOverlappingTimeByUserIdAndTimeRangeAndDailyLogIdNot(
            Long userId,
            DailyLogTimeRange timeRange,
            Long dailyLogId
    ) {
        return dailyLogJpaRepository.existsOverlappingTimeByUserIdAndTimeRangeAndDailyLogIdNot(
                userId,
                timeRange.getStartOn(),
                timeRange.getEndOn(),
                dailyLogId
        );
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
        if (!DAILY_LOG_SORT_PROPERTIES.contains(property)) {
            throw new IllegalArgumentException("Unsupported daily log sort property: " + property);
        }

        return property;
    }
}
