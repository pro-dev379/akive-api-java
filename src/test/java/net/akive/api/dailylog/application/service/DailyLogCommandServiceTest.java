package net.akive.api.dailylog.application.service;

import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.dailylog.application.provided.dto.DailyLogCreateCommand;
import net.akive.api.dailylog.application.required.DailyLogRepository;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.common.exception.business.ResourceConflictError.DAILY_LOG_TIME_OVERLAPS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.dailylog.application.fixture.DailyLogCommandFixture.createDailyLogCreateCommand;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.DAILY_LOG_ID;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class DailyLogCommandServiceTest {

    @Mock
    UserFinder userFinder;

    @Mock
    DailyLogRepository dailyLogRepository;

    @InjectMocks
    DailyLogCommandService dailyLogCommandService;

    @Test
    void 일일_로그를_생성한다() {
        User user = createReconstructedUser(USER_ID);
        DailyLogCreateCommand command = createDailyLogCreateCommand();
        DailyLog savedDailyLog = DailyLog.reconstruct(
                DAILY_LOG_ID,
                user,
                command.focusTime(),
                command.dailyLogType(),
                command.contents()
        );

        given(userFinder.findByUserIdAndNotWithdrawn(command.userId())).willReturn(Optional.of(user));
        given(dailyLogRepository.existsOverlappingTimeByUserIdAndFocusTime(command.userId(), command.focusTime()))
                .willReturn(false);
        given(dailyLogRepository.save(any(DailyLog.class))).willReturn(savedDailyLog);

        Long result = dailyLogCommandService.create(command);

        assertThat(result).isEqualTo(DAILY_LOG_ID);

        ArgumentCaptor<DailyLog> dailyLogCaptor = ArgumentCaptor.forClass(DailyLog.class);
        then(userFinder).should().findByUserIdAndNotWithdrawn(command.userId());
        then(dailyLogRepository).should()
                .existsOverlappingTimeByUserIdAndFocusTime(command.userId(), command.focusTime());
        then(dailyLogRepository).should().save(dailyLogCaptor.capture());

        DailyLog dailyLog = dailyLogCaptor.getValue();
        assertThat(dailyLog.getUser()).isEqualTo(user);
        assertThat(dailyLog.getFocusTime()).isEqualTo(command.focusTime());
        assertThat(dailyLog.getDailyLogType()).isEqualTo(command.dailyLogType());
        assertThat(dailyLog.getContents()).isEqualTo(command.contents());
    }

    @Test
    void 회원이_없으면_일일_로그를_생성할_수_없다() {
        DailyLogCreateCommand command = createDailyLogCreateCommand(UNKNOWN_USER_ID);
        given(userFinder.findByUserIdAndNotWithdrawn(command.userId())).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> dailyLogCommandService.create(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(userFinder).should().findByUserIdAndNotWithdrawn(command.userId());
        then(dailyLogRepository).should(never()).existsOverlappingTimeByUserIdAndFocusTime(any(), any());
        then(dailyLogRepository).should(never()).save(any());
    }

    @Test
    void 시간_범위가_오버랩되는_일일_로그가_있으면_일일_로그를_생성할_수_없다() {
        User user = createReconstructedUser(USER_ID);
        DailyLogCreateCommand command = createDailyLogCreateCommand();

        given(userFinder.findByUserIdAndNotWithdrawn(command.userId())).willReturn(Optional.of(user));
        given(dailyLogRepository.existsOverlappingTimeByUserIdAndFocusTime(command.userId(), command.focusTime()))
                .willReturn(true);

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> dailyLogCommandService.create(command))
                .extracting(ResourceConflictException::getError)
                .isEqualTo(DAILY_LOG_TIME_OVERLAPS);

        then(userFinder).should().findByUserIdAndNotWithdrawn(command.userId());
        then(dailyLogRepository).should()
                .existsOverlappingTimeByUserIdAndFocusTime(command.userId(), command.focusTime());
        then(dailyLogRepository).should(never()).save(any());
    }
}
