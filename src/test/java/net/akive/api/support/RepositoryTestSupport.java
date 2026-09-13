package net.akive.api.support;

import jakarta.persistence.EntityManager;
import net.akive.api.auth.adapter.login.persistence.LegacyUserPasswordJpaRepository;
import net.akive.api.auth.application.login.required.LegacyUserPasswordRepository;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.dailylog.adapter.persistence.DailyLogJpaRepository;
import net.akive.api.dailylog.adapter.persistence.entity.DailyLogEntity;
import net.akive.api.dailylog.application.required.DailyLogRepository;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.dailylog.domain.DailyLogTimeRange;
import net.akive.api.mentoring.adapter.persistence.MentoringProfileJpaRepository;
import net.akive.api.mentoring.application.required.MentoringProfileRepository;
import net.akive.api.plan.application.required.DailyQuantityRepository;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import net.akive.api.plan.application.required.WeeklyQuantityRepository;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.user.application.required.GoalRepository;
import net.akive.api.user.application.required.UserRepository;
import net.akive.api.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.createDailyLog;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.createSessionalPlan;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.FIRST_SUBJECT_NAME;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.createTotalQuantity;
import static net.akive.api.user.domain.fixture.UserFixture.createUser;

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

    @Autowired
    protected MentoringProfileJpaRepository mentoringProfileJpaRepository;

    @Autowired
    protected SessionalPlanRepository sessionalPlanRepository;

    @Autowired
    protected TotalQuantityRepository totalQuantityRepository;

    @Autowired
    protected DailyQuantityRepository dailyQuantityRepository;

    @Autowired
    protected WeeklyQuantityRepository weeklyQuantityRepository;

    @Autowired
    protected DailyLogRepository dailyLogRepository;

    @Autowired
    protected DailyLogJpaRepository dailyLogJpaRepository;

    @Autowired
    protected CurrentTimeProvider currentTimeProvider;

    @Autowired
    protected EntityManager entityManager;

    protected User saveUser() {
        return userRepository.save(createUser());
    }

    protected User saveUser(User user) {
        return userRepository.save(user);
    }

    protected SessionalPlan saveSessionalPlan() {
        SessionalPlan sessionalPlan = createSessionalPlan(saveUser());

        return sessionalPlanRepository.save(sessionalPlan);
    }

    protected SessionalPlan saveSessionalPlan(LocalDateTime requestedAt) {
        SessionalPlan sessionalPlan = createSessionalPlan(saveUser(), requestedAt);

        return sessionalPlanRepository.save(sessionalPlan);
    }

    protected SessionalPlan saveSessionalPlan(String sessionName) {
        return saveSessionalPlan(saveUser(), sessionName);
    }

    protected SessionalPlan saveSessionalPlan(User user, String sessionName) {
        SessionalPlan sessionalPlan = createSessionalPlan(saveUser(user), sessionName);

        return sessionalPlanRepository.save(sessionalPlan);
    }

    protected TotalQuantity saveTotalQuantity() {
        return saveTotalQuantity(FIRST_SUBJECT_NAME);
    }

    protected TotalQuantity saveTotalQuantity(String subjectName) {
        return saveTotalQuantity(saveSessionalPlan(), subjectName);
    }

    protected TotalQuantity saveTotalQuantity(SessionalPlan sessionalPlan, String subjectName) {
        SessionalPlan savedSessionalPlan = sessionalPlanRepository.save(sessionalPlan);

        return totalQuantityRepository.save(createTotalQuantity(savedSessionalPlan, subjectName));
    }

    protected DailyLog saveDailyLog(User user, LocalDate logOn, int startHour) {
        return saveDailyLog(createDailyLog(user, logOn, startHour));
    }

    protected DailyLog saveDailyLog(User user, LocalDate logOn, DailyLogTimeRange timeRange) {
        return saveDailyLog(createDailyLog(user, logOn, timeRange));
    }

    private DailyLog saveDailyLog(DailyLog dailyLog) {
        return dailyLogJpaRepository
                .save(DailyLogEntity.from(dailyLog))
                .to(currentTimeProvider.now());
    }

    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
