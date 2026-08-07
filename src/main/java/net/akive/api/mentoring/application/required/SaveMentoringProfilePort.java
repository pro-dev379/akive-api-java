package net.akive.api.mentoring.application.required;

import net.akive.api.mentoring.domain.MentoringProfile;

public interface SaveMentoringProfilePort {

    MentoringProfile save(MentoringProfile mentoringProfile);
}
