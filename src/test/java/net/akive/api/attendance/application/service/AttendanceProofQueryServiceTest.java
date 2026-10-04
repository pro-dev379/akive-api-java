package net.akive.api.attendance.application.service;

import net.akive.api.attendance.application.required.AttendanceProofRepository;
import net.akive.api.attendance.domain.AttendanceProof;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.attendance.domain.fixture.AttendanceProofFixture.ATTENDANCE_PROOF_ID;
import static net.akive.api.attendance.domain.fixture.AttendanceProofFixture.PROOF_ON;
import static net.akive.api.attendance.domain.fixture.AttendanceProofFixture.createReconstructedAttendanceProof;
import static net.akive.api.common.exception.business.ResourceNotFoundError.ATTENDANCE_PROOF_NOT_FOUND;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AttendanceProofQueryServiceTest {

    @Mock
    AttendanceProofRepository attendanceProofRepository;

    @InjectMocks
    AttendanceProofQueryService attendanceProofQueryService;

    @Test
    void 출석_인증을_조회한다() {
        AttendanceProof attendanceProof = createReconstructedAttendanceProof(
                ATTENDANCE_PROOF_ID,
                PROOF_ON,
                PROOF_ON
        );
        given(attendanceProofRepository.findByUserAndProofOn(USER_ID, PROOF_ON))
                .willReturn(Optional.of(attendanceProof));

        AttendanceProof foundAttendanceProof = attendanceProofQueryService.findOrThrow(USER_ID, PROOF_ON);

        assertThat(foundAttendanceProof).isEqualTo(attendanceProof);
        then(attendanceProofRepository).should().findByUserAndProofOn(USER_ID, PROOF_ON);
    }

    @Test
    void 출석_인증이_없으면_출석_인증_조회에_실패하고_예외를_던진다() {
        given(attendanceProofRepository.findByUserAndProofOn(USER_ID, PROOF_ON))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> attendanceProofQueryService.findOrThrow(USER_ID, PROOF_ON))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(ATTENDANCE_PROOF_NOT_FOUND);

        then(attendanceProofRepository).should().findByUserAndProofOn(USER_ID, PROOF_ON);
    }
}
