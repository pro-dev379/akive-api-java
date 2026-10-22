package net.akive.api.image.adapter.persistence;

import net.akive.api.attendance.domain.AttendanceProof;
import net.akive.api.image.adapter.persistence.entity.ImageAssetEntity;
import net.akive.api.image.application.required.ImageAssetRepository;
import net.akive.api.image.domain.ImageAsset;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;
import static net.akive.api.image.domain.ImageType.ATTENDANCE_PROOF;
import static net.akive.api.image.domain.ImageType.PROFILE_IMAGE;
import static net.akive.api.image.domain.ProcessingStatus.UPLOADING;
import static net.akive.api.image.domain.fixture.ImageAssetFixture.ANOTHER_IMAGE_ASSET_ID;
import static net.akive.api.image.domain.fixture.ImageAssetFixture.IMAGE_ASSET_ID;
import static net.akive.api.user.domain.fixture.UserDetailFixture.AIM;
import static net.akive.api.user.domain.fixture.UserDetailFixture.NICKNAME;
import static net.akive.api.user.domain.fixture.UserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;

class ImageAssetJpaAdapterTest extends RepositoryTestSupport {

    private static final Instant UNREFERENCED_IMAGE_CREATED_BEFORE = Instant.parse("9999-12-31T23:59:59Z");
    private static final int UNREFERENCED_IMAGE_LIMIT = 100;

    @Autowired
    ImageAssetRepository imageAssetRepository;

    @Test
    void 이미지_에셋을_저장한다() {
        ImageAsset imageAsset = ImageAsset.create(IMAGE_ASSET_ID, ATTENDANCE_PROOF);

        ImageAsset savedImageAsset = imageAssetRepository.save(imageAsset);
        flushAndClear();

        ImageAsset foundImageAsset = imageAssetRepository.findByImageAssetId(IMAGE_ASSET_ID).orElseThrow();

        assertThat(foundImageAsset.getImageAssetId()).isEqualTo(IMAGE_ASSET_ID);
        assertThat(foundImageAsset.getImageType()).isEqualTo(ATTENDANCE_PROOF);
        assertThat(foundImageAsset.getProcessingStatus()).isEqualTo(UPLOADING);
        assertThat(foundImageAsset.getMasterObjectKey()).isNull();
        assertThat(foundImageAsset.getActiveRecipeVersion()).isNull();
        assertThat(foundImageAsset.getProcessingError()).isNull();
        assertThat(foundImageAsset.getProcessedAt()).isNull();
        assertThat(foundImageAsset.getVariants()).isEmpty();
        assertThat(savedImageAsset.getCreatedAt()).isNotNull();
    }

    @Test
    void 기존_이미지_에셋을_다시_저장해도_새_이미지_에셋을_생성하지_않는다() {
        imageAssetRepository.save(ImageAsset.create(IMAGE_ASSET_ID, ATTENDANCE_PROOF));
        flushAndClear();

        ImageAsset persistedImageAsset = imageAssetRepository.findByImageAssetId(IMAGE_ASSET_ID).orElseThrow();

        ImageAsset resavedImageAsset = imageAssetRepository.save(persistedImageAsset);
        flushAndClear();

        ImageAsset foundImageAsset = imageAssetRepository.findByImageAssetId(IMAGE_ASSET_ID).orElseThrow();

        assertThat(resavedImageAsset.getImageAssetId()).isEqualTo(IMAGE_ASSET_ID);
        assertThat(resavedImageAsset.getCreatedAt()).isEqualTo(persistedImageAsset.getCreatedAt());
        assertThat(foundImageAsset.getImageAssetId()).isEqualTo(IMAGE_ASSET_ID);
        assertThat(foundImageAsset.getCreatedAt()).isEqualTo(persistedImageAsset.getCreatedAt());
    }

    @Test
    void 이미지_에셋_식별자로_이미지_에셋을_조회한다() {
        ImageAsset savedImageAsset = imageAssetRepository.save(ImageAsset.create(IMAGE_ASSET_ID, ATTENDANCE_PROOF));
        flushAndClear();

        assertThat(imageAssetRepository.findByImageAssetId(IMAGE_ASSET_ID))
                .get()
                .extracting(ImageAsset::getImageAssetId)
                .isEqualTo(savedImageAsset.getImageAssetId());
    }

    @Test
    void 이미지_에셋_식별자로_이미지_에셋을_비관적_쓰기_락으로_조회한다() {
        ImageAsset savedImageAsset = imageAssetRepository.save(ImageAsset.create(IMAGE_ASSET_ID, ATTENDANCE_PROOF));
        flushAndClear();

        assertThat(imageAssetRepository.findByImageAssetIdForUpdate(IMAGE_ASSET_ID))
                .contains(savedImageAsset);
        assertPessimisticWriteLock(IMAGE_ASSET_ID);
    }

    @Test
    void 이미지_에셋을_삭제한다() {
        ImageAsset savedImageAsset = imageAssetRepository.save(ImageAsset.create(IMAGE_ASSET_ID, ATTENDANCE_PROOF));
        flushAndClear();

        imageAssetRepository.delete(savedImageAsset);
        flushAndClear();

        Optional<ImageAsset> foundImageAsset = imageAssetRepository.findByImageAssetId(IMAGE_ASSET_ID);

        assertThat(foundImageAsset).isEmpty();
    }

    @Test
    void 출석에서_참조하지_않는_출석_인증_이미지를_찾아_비관적_쓰기_락으로_조회한다() {
        ImageAsset unusedImageAsset = imageAssetRepository.save(
                ImageAsset.create(ANOTHER_IMAGE_ASSET_ID, ATTENDANCE_PROOF)
        );
        AttendanceProof attendanceProof = saveAttendanceProof();
        UUID referencedImageAssetId = attendanceProof.getProofImageAsset().getImageAssetId();
        flushAndClear();

        List<UUID> imageAssetIds = imageAssetRepository
                .findUnreferencedAttendanceProofImageAssetIdsCreatedBefore(
                        UNREFERENCED_IMAGE_CREATED_BEFORE,
                        UNREFERENCED_IMAGE_LIMIT
                );

        assertThat(imageAssetIds).containsExactly(unusedImageAsset.getImageAssetId());
        assertThat(imageAssetRepository.findUnreferencedAttendanceProofImageAssetByIdForUpdate(
                unusedImageAsset.getImageAssetId()
        )).contains(unusedImageAsset);
        assertPessimisticWriteLock(unusedImageAsset.getImageAssetId());
        assertThat(imageAssetRepository.findUnreferencedAttendanceProofImageAssetByIdForUpdate(
                referencedImageAssetId
        )).isEmpty();
    }

    @Test
    void 회원이_참조하지_않는_프로필_이미지를_찾아_비관적_쓰기_락으로_조회한다() {
        ImageAsset usedImageAsset = imageAssetRepository.save(
                ImageAsset.create(IMAGE_ASSET_ID, PROFILE_IMAGE)
        );
        ImageAsset unusedImageAsset = imageAssetRepository.save(
                ImageAsset.create(ANOTHER_IMAGE_ASSET_ID, PROFILE_IMAGE)
        );
        User user = createUser();
        user.updateUserDetail(NICKNAME, usedImageAsset.getImageAssetId(), AIM);
        userRepository.save(user);
        flushAndClear();

        List<UUID> imageAssetIds = imageAssetRepository.findUnreferencedProfileImageAssetIdsCreatedBefore(
                UNREFERENCED_IMAGE_CREATED_BEFORE,
                UNREFERENCED_IMAGE_LIMIT
        );

        assertThat(imageAssetIds).containsExactly(unusedImageAsset.getImageAssetId());
        assertThat(imageAssetRepository.findUnreferencedProfileImageAssetByIdForUpdate(
                unusedImageAsset.getImageAssetId()
        )).contains(unusedImageAsset);
        assertPessimisticWriteLock(unusedImageAsset.getImageAssetId());
        assertThat(imageAssetRepository.findUnreferencedProfileImageAssetByIdForUpdate(
                usedImageAsset.getImageAssetId()
        )).isEmpty();
    }

    @Test
    void 조회_기준_시각과_같은_미사용_이미지는_조회하지_않는다() {
        ImageAsset imageAsset = imageAssetRepository.save(ImageAsset.create(IMAGE_ASSET_ID, PROFILE_IMAGE));
        flushAndClear();

        List<UUID> imageAssetIds = imageAssetRepository.findUnreferencedProfileImageAssetIdsCreatedBefore(
                imageAsset.getCreatedAt(),
                UNREFERENCED_IMAGE_LIMIT
        );

        assertThat(imageAssetIds).isEmpty();
    }

    private void assertPessimisticWriteLock(UUID imageAssetId) {
        ImageAssetEntity entity = entityManager.find(ImageAssetEntity.class, imageAssetId);
        assertThat(entityManager.getLockMode(entity)).isEqualTo(PESSIMISTIC_WRITE);
    }
}
