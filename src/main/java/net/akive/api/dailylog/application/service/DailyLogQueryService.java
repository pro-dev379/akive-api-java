package net.akive.api.dailylog.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.dailylog.application.provided.DailyLogFinder;
import net.akive.api.dailylog.application.required.DailyLogRepository;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.dailylog.domain.DailyLogType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class DailyLogQueryService implements DailyLogFinder {

    private final DailyLogRepository dailyLogRepository;

    @Override
    public List<DailyLog> findAll(Long userId, LocalDate logOn) {
        return dailyLogRepository.findAllByUserIdAndLogOnOrderByStartOn(userId, logOn);
    }

    @Override
    public PageResult<LocalDate> findLogOns(Long userId, PageCondition pageCondition) {
        return dailyLogRepository.findLogOnsByUserId(userId, pageCondition);
    }

    @Override
    public List<DailyLogType> findDailyLogTypes() {
        return Arrays.stream(DailyLogType.values()).toList();
    }
}
