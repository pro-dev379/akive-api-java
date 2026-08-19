package net.akive.api.support;

import net.akive.api.auth.adapter.login.persistence.LegacyUserPasswordJpaRepository;
import net.akive.api.auth.application.login.required.LegacyUserPasswordRepository;
import net.akive.api.mentoring.application.required.MentoringProfileRepository;
import net.akive.api.user.application.required.GoalRepository;
import net.akive.api.user.application.required.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class RepositoryTestSupport extends IntegrationTestSupport {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected GoalRepository goalRepository;

    @Autowired
    protected LegacyUserPasswordRepository legacyUserPasswordRepository;

    @Autowired
    protected LegacyUserPasswordJpaRepository legacyUserPasswordJpaRepository;

    @Autowired
    protected MentoringProfileRepository mentoringProfileRepository;
}
