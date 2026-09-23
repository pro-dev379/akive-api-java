package net.akive.api.plan.application.service.dailyplan;

import net.akive.api.plan.application.provided.dailyplan.dto.DailyPlansFindQuery;
import net.akive.api.plan.application.required.DailyQuantityRepository;
import net.akive.api.plan.application.required.data.DailyPlanData;
import net.akive.api.plan.application.result.DailyPlan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static net.akive.api.plan.application.fixture.DailyPlanDataFixture.createDailyPlanData;
import static net.akive.api.plan.application.fixture.DailyPlanQueryFixture.createDailyPlansFindQuery;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class DailyPlanQueryServiceTest {

    @Mock
    DailyQuantityRepository dailyQuantityRepository;

    @InjectMocks
    DailyPlanQueryService dailyPlanQueryService;

    @Test
    void 회원_식별자와_날짜로_일일_계획을_조회한다() {
        DailyPlansFindQuery query = createDailyPlansFindQuery();
        DailyPlanData dailyPlanData = createDailyPlanData(query.date());
        given(dailyQuantityRepository.findDailyPlansByUserIdAndDateOrderByPlanHierarchyAsc(
                query.userId(),
                query.date()
        )).willReturn(List.of(dailyPlanData));

        List<DailyPlan> dailyPlans = dailyPlanQueryService.findAll(query);

        assertThat(dailyPlans).hasSize(1);
        DailyPlan dailyPlan = dailyPlans.getFirst();
        assertThat(dailyPlan.sessionalPlanId()).isEqualTo(dailyPlanData.sessionalPlanId());
        assertThat(dailyPlan.totalQuantityId()).isEqualTo(dailyPlanData.totalQuantityId());
        assertThat(dailyPlan.weeklyQuantityId()).isEqualTo(dailyPlanData.weeklyQuantityId());
        assertThat(dailyPlan.subject()).isEqualTo(dailyPlanData.subject());
        assertThat(dailyPlan.dailyQuantity()).isEqualTo(dailyPlanData.dailyQuantity());
        assertThat(dailyPlan.quantityUnit()).isEqualTo(dailyPlanData.quantityUnit());

        then(dailyQuantityRepository).should()
                .findDailyPlansByUserIdAndDateOrderByPlanHierarchyAsc(
                        query.userId(),
                        query.date()
                );
    }
}
