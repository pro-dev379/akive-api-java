package net.akive.api.attendance.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.attendance.application.provided.AttendanceProofSubmitter;
import net.akive.api.attendance.application.provided.dto.AttendanceProofSubmitCommand;
import net.akive.api.attendance.application.required.AttendanceProofRepository;
import net.akive.api.attendance.domain.AttendanceProof;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.file.application.provided.ImageAssetReader;
import net.akive.api.file.application.provided.ImageAssetUploadChecker;
import net.akive.api.file.domain.ImageAsset;
import net.akive.api.user.application.provided.GoalFinder;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.ATTENDANCE_PROOF_IMAGE_NOT_UPLOADED;
import static net.akive.api.common.exception.business.ResourceConflictError.ATTENDANCE_PROOF_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.IMAGE_ASSET_NOT_FOUND;
import static net.akive.api.common.time.ServiceDayPolicy.toServiceDate;
import static net.akive.api.file.domain.ImageType.ATTENDANCE_PROOF;

@Service
@Transactional
@RequiredArgsConstructor
class AttendanceProofCommandService implements AttendanceProofSubmitter {

    private final UserFinder userFinder;
    private final GoalFinder goalFinder;
    private final ImageAssetReader imageAssetReader;
    private final ImageAssetUploadChecker imageAssetUploadChecker;

    private final AttendanceProofRepository attendanceProofRepository;

    private final CurrentTimeProvider currentTimeProvider;

    @Override
    public AttendanceProof submit(AttendanceProofSubmitCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException, ResourceConflictException {
        Instant submittedAt = currentTimeProvider.now();
        LocalDate proofOn = toServiceDate(submittedAt);

        validateNotAlreadySubmitted(command.userId(), proofOn);

        User user = userFinder.findOrThrow(command.userId());
        ImageAsset proofImageAsset = findUploadedProofImageOrThrow(command.proofImageAssetId());
        Goal effectiveGoal = goalFinder.findLatestBefore(command.userId(), proofOn);

        AttendanceProof attendanceProof = AttendanceProof.submit(
                user,
                proofImageAsset,
                effectiveGoal.getAttendOn(),
                submittedAt
        );

        return attendanceProofRepository.save(attendanceProof);
    }

    private void validateNotAlreadySubmitted(Long userId, LocalDate proofOn) {
        if (attendanceProofRepository.existsByUserIdAndProofOn(userId, proofOn)) {
            throw new ResourceConflictException(ATTENDANCE_PROOF_ALREADY_EXISTS);
        }
    }

    private ImageAsset findUploadedProofImageOrThrow(UUID imageAssetId) {
        ImageAsset proofImageAsset = imageAssetReader.find(imageAssetId)
                .filter(imageAsset -> imageAsset.isOfType(ATTENDANCE_PROOF))
                .orElseThrow(() -> new ResourceNotFoundException((IMAGE_ASSET_NOT_FOUND)));

        if (!imageAssetUploadChecker.isOriginalUploaded(imageAssetId, ATTENDANCE_PROOF)) {
            throw new BusinessRuleViolationException(ATTENDANCE_PROOF_IMAGE_NOT_UPLOADED);
        }

        return proofImageAsset;
    }
}
