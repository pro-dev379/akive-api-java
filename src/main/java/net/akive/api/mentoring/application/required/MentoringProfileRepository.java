package net.akive.api.mentoring.application.required;

import net.akive.api.mentoring.domain.MentoringProfile;

public interface MentoringProfileRepository {

    MentoringProfile save(MentoringProfile mentoringProfile);
}
