package net.akive.api.attendance.adapter.persistence;

import net.akive.api.attendance.adapter.persistence.entity.AttendanceProofEntity;
import net.akive.api.attendance.domain.AttendanceProof;
import net.akive.api.image.domain.ImageAsset;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static net.akive.api.attendance.domain.fixture.AttendanceProofFixture.submitAttendanceProof;
import static net.akive.api.common.time.ServiceDayPolicy.toServiceDate;
import static org.assertj.core.api.Assertions.assertThat;

class AttendanceProofJpaAdapterTest extends RepositoryTestSupport {

    @Test
    void 출석_인증을_저장한다() {
        User savedUser = saveUser();
        ImageAsset savedImageAsset = saveAttendanceProofImageAsset();
        AttendanceProof attendanceProof = submitAttendanceProof(savedUser, savedImageAsset);

        AttendanceProof savedAttendanceProof = attendanceProofRepository.save(attendanceProof);
        flushAndClear();

        AttendanceProof foundAttendanceProof = entityManager
                .find(AttendanceProofEntity.class, savedAttendanceProof.getAttendanceProofId())
                .to(toServiceDate(currentTimeProvider.now()));

        assertThat(foundAttendanceProof.getAttendanceProofId())
                .isEqualTo(savedAttendanceProof.getAttendanceProofId());
        assertThat(foundAttendanceProof.getUser().getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(foundAttendanceProof.getProofOn()).isEqualTo(attendanceProof.getProofOn());
        assertThat(foundAttendanceProof.getProofImageAsset().getImageAssetId())
                .isEqualTo(savedImageAsset.getImageAssetId());
        assertThat(foundAttendanceProof.getLate()).isEqualTo(attendanceProof.getLate());
    }

    @Test
    void 회원_식별자와_출석_인증일로_출석_인증을_조회한다() {
        AttendanceProof savedAttendanceProof = saveAttendanceProof();
        flushAndClear();

        Optional<AttendanceProof> foundAttendanceProof = attendanceProofRepository.findByUserAndProofOn(
                savedAttendanceProof.getUser().getUserId(),
                savedAttendanceProof.getProofOn()
        );

        assertThat(foundAttendanceProof).isPresent();
        assertThat(foundAttendanceProof.get().getAttendanceProofId())
                .isEqualTo(savedAttendanceProof.getAttendanceProofId());
        assertThat(foundAttendanceProof.get().getUser().getUserId())
                .isEqualTo(savedAttendanceProof.getUser().getUserId());
        assertThat(foundAttendanceProof.get().getProofOn()).isEqualTo(savedAttendanceProof.getProofOn());
        assertThat(foundAttendanceProof.get().getProofImageAsset().getImageAssetId())
                .isEqualTo(savedAttendanceProof.getProofImageAsset().getImageAssetId());
        assertThat(foundAttendanceProof.get().getLate()).isEqualTo(savedAttendanceProof.getLate());
    }

    @Test
    void 회원_식별자와_출석_인증일로_출석_인증_존재_여부를_확인한다() {
        AttendanceProof savedAttendanceProof = saveAttendanceProof();
        flushAndClear();

        boolean exists = attendanceProofRepository.existsByUserIdAndProofOn(
                savedAttendanceProof.getUser().getUserId(),
                savedAttendanceProof.getProofOn()
        );
        boolean existsOnAnotherDate = attendanceProofRepository.existsByUserIdAndProofOn(
                savedAttendanceProof.getUser().getUserId(),
                savedAttendanceProof.getProofOn().minusDays(1)
        );

        assertThat(exists).isTrue();
        assertThat(existsOnAnotherDate).isFalse();
    }
}
