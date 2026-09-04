package net.akive.api.mentoring.application.provided;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.mentoring.domain.MentoringProfile;

public interface MentoringProfileCreator {

    MentoringProfile create(Long userId) throws ResourceNotFoundException;
}
