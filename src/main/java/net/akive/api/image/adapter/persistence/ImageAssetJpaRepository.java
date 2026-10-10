package net.akive.api.image.adapter.persistence;

import net.akive.api.image.adapter.persistence.entity.ImageAssetEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface ImageAssetJpaRepository extends JpaRepository<ImageAssetEntity, UUID> {

    @Override
    @NotNull
    @EntityGraph(attributePaths = "variants")
    Optional<ImageAssetEntity> findById(@NotNull UUID imageAssetId);
}
