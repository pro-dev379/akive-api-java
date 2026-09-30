package net.akive.api.file.adapter.persistence;

import net.akive.api.file.adapter.persistence.entity.ImageAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface ImageAssetJpaRepository extends JpaRepository<ImageAssetEntity, UUID> {
}
