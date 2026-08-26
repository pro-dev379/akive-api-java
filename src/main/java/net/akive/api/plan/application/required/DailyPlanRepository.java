package net.akive.api.plan.application.required;

import net.akive.api.plan.application.required.data.DailyPlanData;

import java.time.LocalDate;
import java.util.List;

public interface DailyPlanRepository {

    List<DailyPlanData> findDailyPlanDataByUserIdAndDate(Long userId, LocalDate date);
}
