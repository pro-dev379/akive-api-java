package net.akive.api.support;

import net.akive.api.auth.adapter.login.persistence.LegacyUserPasswordJpaRepository;
import net.akive.api.auth.application.login.required.LegacyUserPasswordRepository;
import net.akive.api.mentoring.application.required.MentoringProfileRepository;
import net.akive.api.plan.application.required.*;
import net.akive.api.user.application.required.SaveGoalPort;
import net.akive.api.user.application.required.SaveUserPort;
import net.akive.api.user.application.required.ValidateUserPort;
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
    protected LegacyUserPasswordRepository legacyUserPasswordRepository;

    @Autowired
    protected LegacyUserPasswordJpaRepository legacyUserPasswordJpaRepository;

    @Autowired
    protected MentoringProfileRepository mentoringProfileRepository;

    @Autowired
    protected LoadSessionalPlanPort loadSessionalPlanPort;

    @Autowired
    protected SaveSessionalPlanPort saveSessionalPlanPort;

    @Autowired
    protected DeleteSessionalPlanPort deleteSessionalPlanPort;

    @Autowired
    protected ValidateSessionalPlanPort validateSessionalPlanPort;

    @Autowired
    protected SaveTotalQuantityPort saveTotalQuantityPort;

    @Autowired
    protected LoadTotalQuantityPort loadTotalQuantityPort;

    @Autowired
    protected DeleteTotalQuantityPort deleteTotalQuantityPort;

    @Autowired
    protected ValidateTotalQuantityPort validateTotalQuantityPort;

}
