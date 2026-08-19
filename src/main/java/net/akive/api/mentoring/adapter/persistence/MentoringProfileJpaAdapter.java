package net.akive.api.mentoring.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.mentoring.adapter.persistence.entity.MentoringProfileEntity;
import net.akive.api.mentoring.application.required.MentoringProfileRepository;
import net.akive.api.mentoring.domain.MentoringProfile;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class MentoringProfileJpaAdapter implements MentoringProfileRepository {

    private final MentoringProfileJpaRepository mentoringProfileJpaRepository;

    @Override
    public MentoringProfile save(MentoringProfile mentoringProfile) {
        return mentoringProfileJpaRepository.save(MentoringProfileEntity.from(mentoringProfile)).to();
    }
}
