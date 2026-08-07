package net.akive.api.plan.apapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.required.LoadDailyTodosPort;
import net.akive.api.plan.application.required.dto.DailyTodoData;
import net.akive.api.plan.apapter.persistence.projection.DailyTodoProjection;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DailyTodoJpaAdapter implements LoadDailyTodosPort {

    private final DailyTodoQueryRepository dailyTodoQueryRepository;

    @Override
    public List<DailyTodoData> loadDailyTodosByUserIdAndDate(Long userId, LocalDate date) {
        return dailyTodoQueryRepository
                .findDailyTodosByUserIdAndDate(userId, date)
                .stream()
                .map(this::toDailyTodoData)
                .toList();
    }

    private DailyTodoData toDailyTodoData(DailyTodoProjection projection) {
        return new DailyTodoData(
                projection.sessionalPlanId(),
                projection.totalQuantityId(),
                projection.weeklyQuantityId(),
                projection.dailyQuantityId(),
                projection.subject().to(),
                projection.dailyQuantityAmount().to(),
                projection.unit(),
                projection.date()
        );
    }
}
