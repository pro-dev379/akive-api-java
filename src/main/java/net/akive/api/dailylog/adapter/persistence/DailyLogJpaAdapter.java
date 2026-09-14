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
import net.akive.api.dailylog.domain.FocusTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
    public Optional<DailyLog> findByDailyLogIdAndUserId(Long dailyLogId, Long userId) {
        return dailyLogJpaRepository.findByDailyLogIdAndUser_UserId(dailyLogId, userId)
                .map(entity -> entity.to(currentTimeProvider.now()));
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
    public boolean existsOverlappingTimeByUserIdAndFocusTime(Long userId, FocusTime focusTime) {
        return dailyLogJpaRepository.existsOverlappingTimeByUserIdAndFocusTime(
                userId,
                focusTime.getStartOn(),
                focusTime.getEndOn()
        );
    }

    @Override
    public boolean existsOverlappingTimeByUserIdAndFocusTimeAndDailyLogIdNot(
            Long userId,
            FocusTime focusTime,
            Long dailyLogId
    ) {
        return dailyLogJpaRepository.existsOverlappingTimeByUserIdAndFocusTimeAndDailyLogIdNot(
                userId,
                focusTime.getStartOn(),
                focusTime.getEndOn(),
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
        if (!DAILY_LOG_SORT_PROPERTIES.contains(property)) {
            throw new IllegalArgumentException("Unsupported daily log sort property: " + property);
        }

        return "focusTime." + property;
    }
}
