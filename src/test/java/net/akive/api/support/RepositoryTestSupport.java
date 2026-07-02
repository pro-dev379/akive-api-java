package net.akive.api.support;

import net.akive.api.auth.application.port.out.login.DeleteLegacyUserPasswordPort;
import net.akive.api.auth.application.port.out.login.LoadEmailLoginUserPort;
import net.akive.api.auth.application.port.out.login.LoadLegacyUserPasswordPort;
import net.akive.api.auth.infrastructure.login.LegacyUserPasswordJpaRepository;
import net.akive.api.mentoring.application.port.out.SaveMentoringProfilePort;
import net.akive.api.user.application.port.out.SaveGoalPort;
import net.akive.api.user.application.port.out.SaveUserPort;
import net.akive.api.user.application.port.out.ValidateUserPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class RepositoryTestSupport extends IntegrationTestSupport {

    @Autowired
    protected ValidateUserPort validateUserPort;

    @Autowired
    protected SaveUserPort saveUserPort;

    @Autowired
    protected SaveGoalPort saveGoalPort;

    @Autowired
    protected LoadEmailLoginUserPort loadEmailLoginUserPort;

    @Autowired
    protected LoadLegacyUserPasswordPort loadLegacyUserPasswordPort;

    @Autowired
    protected DeleteLegacyUserPasswordPort deleteLegacyUserPasswordPort;

    @Autowired
    protected LegacyUserPasswordJpaRepository legacyUserPasswordJpaRepository;

    @Autowired
    protected SaveMentoringProfilePort saveMentoringProfilePort;


}
