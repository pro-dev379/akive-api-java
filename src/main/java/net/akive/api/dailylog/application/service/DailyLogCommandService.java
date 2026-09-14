package net.akive.api.dailylog.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.dailylog.application.provided.DailyLogCreator;
import net.akive.api.dailylog.application.provided.dto.DailyLogCreateCommand;
import net.akive.api.dailylog.application.provided.dto.DailyLogDeleteCommand;
import net.akive.api.dailylog.application.required.DailyLogRepository;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.dailylog.domain.FocusTime;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.ResourceConflictError.DAILY_LOG_TIME_OVERLAPS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.DAILY_LOG_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
class DailyLogCommandService implements DailyLogCreator {

    private final UserFinder userFinder;
    private final DailyLogRepository dailyLogRepository;

    @Override
    public Long create(DailyLogCreateCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException, ResourceConflictException {
        User user = findUserOrThrow(command.userId());

        validateTimeNotOverlapping(command.userId(), command.focusTime());

        DailyLog dailyLog = DailyLog.create(
                user,
                command.focusTime(),
                command.dailyLogType(),
                command.contents()
        );

        DailyLog savedDailyLog = dailyLogRepository.save(dailyLog);

        return savedDailyLog.getDailyLogId();
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

    private void validateTimeNotOverlapping(Long userId, FocusTime focusTime) {
        if (dailyLogRepository.existsOverlappingTimeByUserIdAndFocusTime(userId, focusTime)) {
            throw new ResourceConflictException(DAILY_LOG_TIME_OVERLAPS);
        }
    }
}
