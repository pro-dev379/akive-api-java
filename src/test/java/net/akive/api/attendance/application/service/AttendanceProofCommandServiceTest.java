package net.akive.api.attendance.application.service;

import net.akive.api.attendance.application.provided.dto.AttendanceProofSubmitCommand;
import net.akive.api.attendance.application.required.AttendanceProofRepository;
import net.akive.api.attendance.domain.AttendanceProof;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.image.application.provided.ImageAssetDeleter;
import net.akive.api.image.application.provided.ImageAssetReader;
import net.akive.api.image.application.provided.ImageAssetUploadChecker;
import net.akive.api.image.domain.ImageAsset;
import net.akive.api.user.application.provided.GoalFinder;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static net.akive.api.attendance.application.fixture.AttendanceProofCommandFixture.createAttendanceProofSubmitCommand;
import static net.akive.api.attendance.domain.fixture.AttendanceProofFixture.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.ATTENDANCE_PROOF_IMAGE_NOT_UPLOADED;
import static net.akive.api.common.exception.business.ResourceConflictError.ATTENDANCE_PROOF_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.*;
import static net.akive.api.common.time.ServiceDayPolicy.toInstantAtServiceZone;
import static net.akive.api.common.time.ServiceDayPolicy.toServiceDate;
import static net.akive.api.image.domain.ImageType.ATTENDANCE_PROOF;
import static net.akive.api.image.domain.ImageType.PROFILE_IMAGE;
import static net.akive.api.image.domain.fixture.ImageAssetFixture.IMAGE_ASSET_ID;
import static net.akive.api.image.domain.fixture.ImageAssetFixture.UNKNOWN_IMAGE_ASSET_ID;
import static net.akive.api.user.domain.UserPolicy.*;
import static net.akive.api.user.domain.fixture.GoalFixture.GOAL_ID;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AttendanceProofCommandServiceTest {

    private static final Instant SUBMITTED_AT = toInstantAtServiceZone(
            LocalDateTime.of(2026, 10, 1, 9, 1)
    );
    private static final LocalDate EXPECTED_PROOF_ON = toServiceDate(SUBMITTED_AT);

    @Mock
    UserFinder userFinder;

    @Mock
    GoalFinder goalFinder;

    @Mock
    ImageAssetReader imageAssetReader;

    @Mock
    ImageAssetDeleter imageAssetDeleter;

    @Mock
    ImageAssetUploadChecker imageAssetUploadChecker;

    @Mock
    AttendanceProofRepository attendanceProofRepository;

    @Mock
    CurrentTimeProvider currentTimeProvider;

    @InjectMocks
    AttendanceProofCommandService attendanceProofCommandService;

    @Test
    void 서비스일에_적용되는_목표를_기준으로_출석_인증을_생성_및_제출한다() {
        User user = createReconstructedUser(USER_ID);
        ImageAsset imageAsset = createAttendanceProofImageAsset();
        Goal effectiveGoal = Goal.reconstruct(
                GOAL_ID,
                user,
                GOAL_ATTEND_ON,
                DEFAULT_GOAL_REST_10_MIN_SEC,
                DEFAULT_GOAL_MEAL_NUM,
                DEFAULT_GOAL_MEAL_1_TIME_SEC
        );
        AttendanceProofSubmitCommand command = createAttendanceProofSubmitCommand();

        given(imageAssetReader.findForUpdate(IMAGE_ASSET_ID)).willReturn(Optional.of(imageAsset));
        given(userFinder.findOrThrow(USER_ID)).willReturn(user);
        given(currentTimeProvider.now()).willReturn(SUBMITTED_AT);
        given(attendanceProofRepository.existsByUserIdAndProofOn(USER_ID, EXPECTED_PROOF_ON)).willReturn(false);
        given(imageAssetUploadChecker.isOriginalImageUploaded(IMAGE_ASSET_ID, ATTENDANCE_PROOF)).willReturn(true);
        given(goalFinder.findLatestBefore(USER_ID, EXPECTED_PROOF_ON)).willReturn(effectiveGoal);
        given(attendanceProofRepository.save(any(AttendanceProof.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        AttendanceProof attendanceProof = attendanceProofCommandService.submit(command);

        assertThat(attendanceProof.getUser()).isEqualTo(user);
        assertThat(attendanceProof.getProofImageAsset()).isEqualTo(imageAsset);
        assertThat(attendanceProof.getProofOn()).isEqualTo(EXPECTED_PROOF_ON);
        assertThat(attendanceProof.getLate()).isTrue();

        then(attendanceProofRepository).should().existsByUserIdAndProofOn(USER_ID, EXPECTED_PROOF_ON);
        then(imageAssetUploadChecker).should().isOriginalImageUploaded(IMAGE_ASSET_ID, ATTENDANCE_PROOF);
        then(goalFinder).should().findLatestBefore(USER_ID, EXPECTED_PROOF_ON);
        then(attendanceProofRepository).should().save(attendanceProof);
    }

    @Test
    void 같은_서비스일의_출석_인증이_이미_존재하면_출석_인증을_할_수_없다() {
        AttendanceProofSubmitCommand command = createAttendanceProofSubmitCommand();

        given(currentTimeProvider.now()).willReturn(SUBMITTED_AT);
        given(attendanceProofRepository.existsByUserIdAndProofOn(USER_ID, EXPECTED_PROOF_ON)).willReturn(true);

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> attendanceProofCommandService.submit(command))
                .extracting(ResourceConflictException::getError)
                .isEqualTo(ATTENDANCE_PROOF_ALREADY_EXISTS);

        then(attendanceProofRepository).should().existsByUserIdAndProofOn(USER_ID, EXPECTED_PROOF_ON);
        then(userFinder).shouldHaveNoInteractions();
        then(imageAssetReader).shouldHaveNoInteractions();
        then(imageAssetUploadChecker).shouldHaveNoInteractions();
        then(goalFinder).shouldHaveNoInteractions();
        then(attendanceProofRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void 사용자가_없으면_출석_인증을_할_수_없다() {
        AttendanceProofSubmitCommand command = createAttendanceProofSubmitCommand(UNKNOWN_USER_ID);

        given(currentTimeProvider.now()).willReturn(SUBMITTED_AT);
        given(userFinder.findOrThrow(UNKNOWN_USER_ID)).willThrow(new ResourceNotFoundException(USER_NOT_FOUND));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> attendanceProofCommandService.submit(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(attendanceProofRepository).should().existsByUserIdAndProofOn(UNKNOWN_USER_ID, EXPECTED_PROOF_ON);
        then(imageAssetReader).shouldHaveNoInteractions();
        then(imageAssetUploadChecker).shouldHaveNoInteractions();
        then(goalFinder).shouldHaveNoInteractions();
        then(attendanceProofRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void 이미지_에셋이_없으면_출석_인증을_할_수_없다() {
        User user = createReconstructedUser(USER_ID);
        AttendanceProofSubmitCommand command = createAttendanceProofSubmitCommand(UNKNOWN_IMAGE_ASSET_ID);

        given(currentTimeProvider.now()).willReturn(SUBMITTED_AT);
        given(userFinder.findOrThrow(USER_ID)).willReturn(user);
        given(imageAssetReader.findForUpdate(UNKNOWN_IMAGE_ASSET_ID)).willReturn(Optional.empty());

        assertImageAssetNotFound(command);

        then(attendanceProofRepository).should().existsByUserIdAndProofOn(USER_ID, EXPECTED_PROOF_ON);
        then(imageAssetUploadChecker).shouldHaveNoInteractions();
        then(goalFinder).shouldHaveNoInteractions();
        then(attendanceProofRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void 출석_인증용이_아닌_이미지_에셋으로는_출석_인증을_할_수_없다() {
        User user = createReconstructedUser(USER_ID);
        ImageAsset imageAsset = ImageAsset.create(IMAGE_ASSET_ID, PROFILE_IMAGE);
        AttendanceProofSubmitCommand command = createAttendanceProofSubmitCommand();

        given(currentTimeProvider.now()).willReturn(SUBMITTED_AT);
        given(userFinder.findOrThrow(USER_ID)).willReturn(user);
        given(imageAssetReader.findForUpdate(IMAGE_ASSET_ID)).willReturn(Optional.of(imageAsset));

        assertImageAssetNotFound(command);

        then(attendanceProofRepository).should().existsByUserIdAndProofOn(USER_ID, EXPECTED_PROOF_ON);
        then(imageAssetUploadChecker).shouldHaveNoInteractions();
        then(goalFinder).shouldHaveNoInteractions();
        then(attendanceProofRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void 원본_이미지가_업로드되지_않으면_출석_인증을_할_수_없다() {
        User user = createReconstructedUser(USER_ID);
        ImageAsset imageAsset = createAttendanceProofImageAsset();
        AttendanceProofSubmitCommand command = createAttendanceProofSubmitCommand();

        given(currentTimeProvider.now()).willReturn(SUBMITTED_AT);
        given(userFinder.findOrThrow(USER_ID)).willReturn(user);
        given(imageAssetReader.findForUpdate(IMAGE_ASSET_ID)).willReturn(Optional.of(imageAsset));
        given(imageAssetUploadChecker.isOriginalImageUploaded(IMAGE_ASSET_ID, ATTENDANCE_PROOF)).willReturn(false);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> attendanceProofCommandService.submit(command))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(ATTENDANCE_PROOF_IMAGE_NOT_UPLOADED);

        then(attendanceProofRepository).should().existsByUserIdAndProofOn(USER_ID, EXPECTED_PROOF_ON);
        then(goalFinder).shouldHaveNoInteractions();
        then(attendanceProofRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void 서비스일에_적용되는_목표가_없으면_출석_인증을_할_수_없다() {
        User user = createReconstructedUser(USER_ID);
        ImageAsset imageAsset = createAttendanceProofImageAsset();
        AttendanceProofSubmitCommand command = createAttendanceProofSubmitCommand();

        given(currentTimeProvider.now()).willReturn(SUBMITTED_AT);
        given(userFinder.findOrThrow(USER_ID)).willReturn(user);
        given(imageAssetReader.findForUpdate(IMAGE_ASSET_ID)).willReturn(Optional.of(imageAsset));
        given(imageAssetUploadChecker.isOriginalImageUploaded(IMAGE_ASSET_ID, ATTENDANCE_PROOF)).willReturn(true);
        given(goalFinder.findLatestBefore(USER_ID, EXPECTED_PROOF_ON))
                .willThrow(new ResourceNotFoundException(GOAL_NOT_FOUND));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> attendanceProofCommandService.submit(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(GOAL_NOT_FOUND);

        then(attendanceProofRepository).should().existsByUserIdAndProofOn(USER_ID, EXPECTED_PROOF_ON);
        then(attendanceProofRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void 출석_인증을_취소하면_출석_인증과_이미지_에셋을_삭제한다() {
        AttendanceProof attendanceProof = createReconstructedAttendanceProof(false);
        given(attendanceProofRepository.findByUserAndProofOn(USER_ID, PROOF_ON))
                .willReturn(Optional.of(attendanceProof));

        attendanceProofCommandService.cancel(USER_ID, PROOF_ON);

        then(attendanceProofRepository).should().findByUserAndProofOn(USER_ID, PROOF_ON);
        then(attendanceProofRepository).should().delete(attendanceProof);
        then(imageAssetDeleter).should().delete(attendanceProof.getProofImageAsset().getImageAssetId());
    }

    @Test
    void 출석_인증이_없으면_출석_인증을_취소할_수_없다() {
        given(attendanceProofRepository.findByUserAndProofOn(USER_ID, PROOF_ON))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> attendanceProofCommandService.cancel(USER_ID, PROOF_ON))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(ATTENDANCE_PROOF_NOT_FOUND);

        then(attendanceProofRepository).should().findByUserAndProofOn(USER_ID, PROOF_ON);
        then(attendanceProofRepository).shouldHaveNoMoreInteractions();
        then(imageAssetDeleter).shouldHaveNoInteractions();
    }

    private void assertImageAssetNotFound(AttendanceProofSubmitCommand command) {
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> attendanceProofCommandService.submit(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(IMAGE_ASSET_NOT_FOUND);
    }
}
