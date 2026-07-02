package net.akive.api.mentoring.infrastructure;

import lombok.RequiredArgsConstructor;
import net.akive.api.mentoring.application.port.out.SaveMentoringProfilePort;
import net.akive.api.mentoring.domain.MentoringProfile;
import net.akive.api.mentoring.infrastructure.entity.MentoringProfileEntity;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class MentoringProfileJpaAdapter implements SaveMentoringProfilePort {

    private final MentoringProfileJpaRepository mentoringProfileJpaRepository;

    @Override
    public MentoringProfile save(MentoringProfile mentoringProfile) {
        return mentoringProfileJpaRepository
                .save(MentoringProfileEntity.fromModel(mentoringProfile))
                .toModel();
    }
}
