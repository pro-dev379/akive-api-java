package net.akive.api.mentoring.adapter.persistence;

import net.akive.api.mentoring.adapter.persistence.entity.MentoringProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentoringProfileJpaRepository extends JpaRepository<MentoringProfileEntity, Long> {
}
