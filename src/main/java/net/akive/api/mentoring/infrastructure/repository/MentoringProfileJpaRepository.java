package net.akive.api.mentoring.infrastructure.repository;

import net.akive.api.mentoring.infrastructure.entity.MentoringProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentoringProfileJpaRepository extends JpaRepository<MentoringProfileEntity, Long> {
}
