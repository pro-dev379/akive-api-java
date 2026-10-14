package net.akive.api.coaching.application.required;

import net.akive.api.coaching.application.prompt.CoachingPromptComposition;

import java.util.List;

public interface CoachingContextProvider {

    List<CoachingPromptComposition.ContextSection> findContextSections(Long userId, String message);
}
