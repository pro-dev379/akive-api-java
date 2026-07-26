package net.akive.api.support;

import net.akive.api.auth.application.port.out.login.DeleteLegacyUserPasswordPort;
import net.akive.api.auth.application.port.out.login.LoadEmailLoginUserPort;
import net.akive.api.auth.application.port.out.login.LoadLegacyUserPasswordPort;
import net.akive.api.auth.infrastructure.repository.LegacyUserPasswordJpaRepository;
import net.akive.api.mentoring.application.port.out.SaveMentoringProfilePort;
import net.akive.api.plan.application.port.out.*;
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
