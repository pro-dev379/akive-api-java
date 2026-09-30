package net.akive.api.file.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.file.adapter.persistence.entity.ImageAssetEntity;
import net.akive.api.file.application.required.ImageAssetRepository;
import net.akive.api.file.domain.ImageAsset;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class ImageAssetJpaAdapter implements ImageAssetRepository {

    private final ImageAssetJpaRepository imageAssetJpaRepository;

    @Override
    public ImageAsset save(ImageAsset imageAsset) {
        return imageAssetJpaRepository.save(ImageAssetEntity.from(imageAsset)).to();
    }

    @Override
    public Optional<ImageAsset> findByImageAssetId(UUID imageAssetId) {
        return imageAssetJpaRepository.findById(imageAssetId).map(ImageAssetEntity::to);
    }

    @Override
    public void delete(ImageAsset imageAsset) {
        imageAssetJpaRepository.delete(ImageAssetEntity.from(imageAsset));
    }
}
