package net.akive.api.mentoring.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.mentoring.application.port.in.CreateDefaultMentoringProfileUseCase;
import net.akive.api.mentoring.application.port.out.MentoringProfileRepository;
import net.akive.api.mentoring.domain.MentoringProfile;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class CreateDefaultMentoringProfileService implements CreateDefaultMentoringProfileUseCase {

    private final MentoringProfileRepository mentoringProfileRepository;

    @Override
    public void create(Long userId) {
        mentoringProfileRepository.save(
                MentoringProfile.createDefault(userId)
        );
    }
}
