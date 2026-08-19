package net.akive.api.mentoring.application.provided;

import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.mentoring.domain.MentoringProfile;

public interface MentoringProfileCreator {

    MentoringProfile create(Long userId) throws NotFoundException;
}
