package net.akive.api.attendance.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.image.domain.ImageAsset;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;

import static net.akive.api.attendance.domain.fixture.AttendanceProofFixture.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.ATTENDANCE_PROOF_DATE_IN_FUTURE;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.ATTENDANCE_PROOF_NOT_YET_ALLOWED;
import static net.akive.api.common.time.ServiceDayPolicy.toInstantAtServiceZone;
import static net.akive.api.user.domain.fixture.UserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AttendanceProofTest {

    @Test
    void 저장된_출석_인증을_복원한다() {
        User user = createUser();
        ImageAsset proofImageAsset = createAttendanceProofImageAsset();

        AttendanceProof attendanceProof = createReconstructedAttendanceProof(
                ATTENDANCE_PROOF_ID,
                user,
                PROOF_ON,
                proofImageAsset,
                true,
                PROOF_ON
        );

        assertThat(attendanceProof.getAttendanceProofId()).isEqualTo(ATTENDANCE_PROOF_ID);
        assertThat(attendanceProof.getUser()).isEqualTo(user);
        assertThat(attendanceProof.getProofOn()).isEqualTo(PROOF_ON);
        assertThat(attendanceProof.getProofImageAsset()).isEqualTo(proofImageAsset);
        assertThat(attendanceProof.getLate()).isTrue();
    }

    @Test
    void 출석_인증일이_미래이면_출석_인증을_복원할_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> createReconstructedAttendanceProof(
                        ATTENDANCE_PROOF_ID,
                        PROOF_ON,
                        PROOF_ON.minusDays(1)
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(ATTENDANCE_PROOF_DATE_IN_FUTURE);
    }

    @Test
    void 목표_출석_시간_5분_전부터_출석을_인증할_수_있다() {
        Instant submittedAt = toInstantAtServiceZone(PROOF_ON.atTime(GOAL_ATTEND_ON).minusMinutes(5));

        AttendanceProof attendanceProof = submitAttendanceProof(GOAL_ATTEND_ON, submittedAt);

        assertThat(attendanceProof.getAttendanceProofId()).isNull();
        assertThat(attendanceProof.getProofOn()).isEqualTo(PROOF_ON);
        assertThat(attendanceProof.getLate()).isFalse();
    }

    @Test
    void 목표_출석_시간_5분_전보다_이르면_출석을_인증할_수_없다() {
        Instant submittedAt = toInstantAtServiceZone(
                PROOF_ON.atTime(GOAL_ATTEND_ON).minusMinutes(5).minusNanos(1)
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> submitAttendanceProof(GOAL_ATTEND_ON, submittedAt))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(ATTENDANCE_PROOF_NOT_YET_ALLOWED);
    }

    @Test
    void 목표_출석_시간까지는_지각이_아니다() {
        Instant submittedAt = toInstantAtServiceZone(PROOF_ON.atTime(GOAL_ATTEND_ON));

        AttendanceProof attendanceProof = submitAttendanceProof(GOAL_ATTEND_ON, submittedAt);

        assertThat(attendanceProof.getLate()).isFalse();
    }

    @Test
    void 목표_출석_시간을_지나면_지각이다() {
        Instant submittedAt = toInstantAtServiceZone(PROOF_ON.atTime(GOAL_ATTEND_ON)).plusNanos(1);

        AttendanceProof attendanceProof = submitAttendanceProof(GOAL_ATTEND_ON, submittedAt);

        assertThat(attendanceProof.getLate()).isTrue();
    }

    @Test
    void 목표_출석_시간이_일간_경계_시간_이전이면_다음_날을_기준으로_지각_여부를_판정한다() {
        LocalTime goalAttendOn = LocalTime.of(2, 0);
        Instant submittedAt = toInstantAtServiceZone(PROOF_ON.plusDays(1).atTime(1, 55));

        AttendanceProof attendanceProof = submitAttendanceProof(goalAttendOn, submittedAt);

        assertThat(attendanceProof.getProofOn()).isEqualTo(PROOF_ON);
        assertThat(attendanceProof.getLate()).isFalse();
    }

    @Test
    void 목표_출석_시간이_5시_5분이면_같은_서비스일의_5시부터_출석을_인증할_수_있다() {
        LocalTime goalAttendOn = LocalTime.of(5, 5);
        Instant submittedAt = toInstantAtServiceZone(PROOF_ON.atTime(5, 0));

        AttendanceProof attendanceProof = submitAttendanceProof(goalAttendOn, submittedAt);

        assertThat(attendanceProof.getProofOn()).isEqualTo(PROOF_ON);
        assertThat(attendanceProof.getLate()).isFalse();
    }

    @Test
    void 같은_식별자를_가진_출석_인증은_같게_취급한다() {
        AttendanceProof attendanceProof = createReconstructedAttendanceProof(
                ATTENDANCE_PROOF_ID,
                PROOF_ON,
                PROOF_ON
        );
        AttendanceProof sameAttendanceProof = createReconstructedAttendanceProof(
                ATTENDANCE_PROOF_ID,
                PROOF_ON.minusDays(1),
                PROOF_ON
        );

        assertThat(attendanceProof).isEqualTo(sameAttendanceProof);
        assertThat(attendanceProof.hashCode()).isEqualTo(sameAttendanceProof.hashCode());
    }

    @Test
    void 식별자가_다른_출석_인증은_다르게_취급한다() {
        AttendanceProof attendanceProof = createReconstructedAttendanceProof(
                ATTENDANCE_PROOF_ID,
                PROOF_ON,
                PROOF_ON
        );
        AttendanceProof anotherAttendanceProof = createReconstructedAttendanceProof(
                ANOTHER_ATTENDANCE_PROOF_ID,
                PROOF_ON,
                PROOF_ON
        );

        assertThat(attendanceProof).isNotEqualTo(anotherAttendanceProof);
    }

    @Test
    void 식별자가_없는_출석_인증끼리는_나머지_값이_같더라도_다르게_취급한다() {
        Instant submittedAt = toInstantAtServiceZone(PROOF_ON.atTime(GOAL_ATTEND_ON));
        User user = createUser();
        ImageAsset proofImageAsset = createAttendanceProofImageAsset();
        AttendanceProof attendanceProof = submitAttendanceProof(
                user,
                proofImageAsset,
                GOAL_ATTEND_ON,
                submittedAt
        );
        AttendanceProof anotherAttendanceProof = submitAttendanceProof(
                user,
                proofImageAsset,
                GOAL_ATTEND_ON,
                submittedAt
        );

        assertThat(attendanceProof).isNotEqualTo(anotherAttendanceProof);
    }
}
