package net.akive.api.dailylog.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.dailylog.application.provided.DailyLogCreator;
import net.akive.api.dailylog.application.provided.dto.DailyLogCreateCommand;
import net.akive.api.dailylog.application.provided.dto.DailyLogDeleteCommand;
import net.akive.api.dailylog.application.provided.dto.DailyLogUpdateCommand;
import net.akive.api.dailylog.application.required.DailyLogRepository;
import net.akive.api.dailylog.domain.ActivityTime;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

import static net.akive.api.common.exception.business.ResourceConflictError.DAILY_LOG_TIME_OVERLAPS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.DAILY_LOG_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.common.time.ServiceDayPolicy.toKoreanDate;

@Service
@Transactional
@RequiredArgsConstructor
class DailyLogCommandService implements DailyLogCreator {

    private final UserFinder userFinder;
    private final DailyLogRepository dailyLogRepository;

    private final CurrentTimeProvider currentTimeProvider;

    @Override
    public Long create(DailyLogCreateCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException, ResourceConflictException {
        Instant requestedAt = currentTimeProvider.now();
        LocalDate currentDate = toKoreanDate(requestedAt);
        ActivityTime activityTime = command.activityTime().to(requestedAt);
        User user = findUserOrThrow(command.userId());

        validateTimeNotOverlapping(command.userId(), activityTime);

        DailyLog dailyLog = DailyLog.create(
                user,
                activityTime,
                command.dailyLogType(),
                command.contents(),
                currentDate
        );

        DailyLog savedDailyLog = dailyLogRepository.save(dailyLog);

        return savedDailyLog.getDailyLogId();
    }

    @Override
    public void update(DailyLogUpdateCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException, ResourceConflictException {
        Instant requestedAt = currentTimeProvider.now();
        LocalDate currentDate = toKoreanDate(requestedAt);
        ActivityTime targetActivityTime = command.targetActivityTime().to(requestedAt);
        DailyLog dailyLog = findDailyLogOrThrow(command.dailyLogId(), command.userId());

        validateTimeNotOverlappingExcludingSelf(
                command.userId(),
                targetActivityTime,
                command.dailyLogId()
        );

        dailyLog.update(
                targetActivityTime,
                command.targetDailyLogType(),
                command.targetContents(),
                currentDate
        );

        dailyLogRepository.save(dailyLog);
    }

    @Override
    public void delete(DailyLogDeleteCommand command) throws ResourceNotFoundException {
        DailyLog dailyLog = findDailyLogOrThrow(command.dailyLogId(), command.userId());

        dailyLogRepository.delete(dailyLog);
    }

    private User findUserOrThrow(Long userId) {
        return userFinder.findByUserIdAndNotWithdrawn(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
    }

    private DailyLog findDailyLogOrThrow(Long dailyLogId, Long userId) {
        return dailyLogRepository.findByDailyLogIdAndUserId(dailyLogId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(DAILY_LOG_NOT_FOUND));
    }

    private void validateTimeNotOverlapping(Long userId, ActivityTime activityTime) {
        if (dailyLogRepository.existsOverlappingTimeByUserIdAndActivityTime(userId, activityTime)) {
            throw new ResourceConflictException(DAILY_LOG_TIME_OVERLAPS);
        }
    }

    private void validateTimeNotOverlappingExcludingSelf(Long userId, ActivityTime activityTime, Long dailyLogId) {
        boolean isOverlapping = dailyLogRepository.existsOverlappingTimeByUserIdAndActivityTimeAndDailyLogIdNot(
                userId,
                activityTime,
                dailyLogId
        );

        if (isOverlapping) {
            throw new ResourceConflictException(DAILY_LOG_TIME_OVERLAPS);
        }
    }
}
