package net.akive.api.file.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.file.adapter.persistence.entity.ImageAssetEntity;
import net.akive.api.file.application.required.ImageAssetRepository;
import net.akive.api.file.domain.ImageAsset;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class ImageAssetJpaAdapter implements ImageAssetRepository {

    private final ImageAssetJpaRepository imageAssetJpaRepository;

    @Override
    public ImageAsset save(ImageAsset imageAsset) {
        return imageAssetJpaRepository.save(ImageAssetEntity.from(imageAsset)).to();
    }
}
