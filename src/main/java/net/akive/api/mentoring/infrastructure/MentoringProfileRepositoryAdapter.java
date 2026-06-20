package net.akive.api.mentoring.infrastructure;

import lombok.RequiredArgsConstructor;
import net.akive.api.mentoring.application.port.out.MentoringProfileRepository;
import net.akive.api.mentoring.domain.MentoringProfile;
import net.akive.api.mentoring.infrastructure.entity.MentoringProfileEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class MentoringProfileRepositoryAdapter implements MentoringProfileRepository {

    private final MentoringProfileJpaRepository mentoringProfileJpaRepository;

    @Override
    public MentoringProfile save(MentoringProfile mentoringProfile) {
        return mentoringProfileJpaRepository
                .save(MentoringProfileEntity.fromModel(mentoringProfile))
                .toModel();
    }
}
