package net.akive.api.mentoring.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.mentoring.application.provided.MentoringProfileCreator;
import net.akive.api.mentoring.application.required.MentoringProfileRepository;
import net.akive.api.mentoring.domain.MentoringProfile;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
class MentoringProfileService implements MentoringProfileCreator {

    private final UserFinder userFinder;
    private final MentoringProfileRepository mentoringProfileRepository;

    @Override
    public MentoringProfile create(Long userId) {
        User user = userFinder.find(userId);
        MentoringProfile mentoringProfile = MentoringProfile.create(user);

        return mentoringProfileRepository.save(mentoringProfile);
    }
}
