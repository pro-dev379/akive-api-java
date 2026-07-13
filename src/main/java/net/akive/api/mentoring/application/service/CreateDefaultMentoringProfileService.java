package net.akive.api.mentoring.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.mentoring.application.port.in.CreateDefaultMentoringProfileUseCase;
import net.akive.api.mentoring.application.port.out.SaveMentoringProfilePort;
import net.akive.api.mentoring.domain.MentoringProfile;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class CreateDefaultMentoringProfileService implements CreateDefaultMentoringProfileUseCase {

    private final SaveMentoringProfilePort saveMentoringProfilePort;

    @Override
    public void create(Long userId) {
        saveMentoringProfilePort.save(
                MentoringProfile.createDefault(userId)
        );
    }
}
