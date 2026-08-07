package net.akive.api.support;

import net.akive.api.auth.application.login.required.DeleteLegacyUserPasswordPort;
import net.akive.api.auth.application.login.required.LoadEmailLoginUserPort;
import net.akive.api.auth.application.login.required.LoadLegacyUserPasswordPort;
import net.akive.api.auth.adapter.login.persistence.LegacyUserPasswordJpaRepository;
import net.akive.api.mentoring.application.required.SaveMentoringProfilePort;
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
    protected LoadEmailLoginUserPort loadEmailLoginUserPort;

    @Autowired
    protected LoadLegacyUserPasswordPort loadLegacyUserPasswordPort;

    @Autowired
    protected DeleteLegacyUserPasswordPort deleteLegacyUserPasswordPort;

    @Autowired
    protected LegacyUserPasswordJpaRepository legacyUserPasswordJpaRepository;

    @Autowired
    protected SaveMentoringProfilePort saveMentoringProfilePort;

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
