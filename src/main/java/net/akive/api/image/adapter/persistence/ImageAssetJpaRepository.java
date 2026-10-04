package net.akive.api.image.adapter.persistence;

import net.akive.api.image.adapter.persistence.entity.ImageAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface ImageAssetJpaRepository extends JpaRepository<ImageAssetEntity, UUID> {
}
