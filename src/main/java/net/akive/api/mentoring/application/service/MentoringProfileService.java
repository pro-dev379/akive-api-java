package net.akive.api.mentoring.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.mentoring.application.provided.MentoringProfileCreator;
import net.akive.api.mentoring.application.required.MentoringProfileRepository;
import net.akive.api.mentoring.domain.MentoringProfile;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.NotFoundError.USER_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
class MentoringProfileService implements MentoringProfileCreator {

    private final UserFinder userFinder;
    private final MentoringProfileRepository mentoringProfileRepository;

    @Override
    public MentoringProfile create(Long userId) throws NotFoundException {
        User user = userFinder.findByUserIdAndLeaveFalse(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        MentoringProfile mentoringProfile = MentoringProfile.create(user);

        return mentoringProfileRepository.save(mentoringProfile);
    }
}
