package net.akive.api.user.application.required;

import net.akive.api.user.domain.Goal;

public interface SaveGoalPort {

    Goal save(Goal goal);
}
