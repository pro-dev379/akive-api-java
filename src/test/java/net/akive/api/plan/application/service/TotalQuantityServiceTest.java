package net.akive.api.plan.application.service;

import net.akive.api.plan.application.dto.command.CreateTotalQuantityCommand;
import net.akive.api.plan.application.port.out.LoadSessionalPlanPort;
import net.akive.api.plan.application.port.out.SaveTotalQuantityPort;
import net.akive.api.plan.domain.SessionPeriod;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.domain.Subject;
import net.akive.api.plan.domain.TotalQuantityAmount;
import net.akive.api.plan.domain.TotalQuantityPlan;
import net.akive.api.plan.exception.PlanException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static net.akive.api.plan.exception.PlanErrorCode.SESSIONAL_PLAN_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TotalQuantityServiceTest {

    @Mock
    LoadSessionalPlanPort loadSessionalPlanPort;

    @Mock
    SaveTotalQuantityPort saveTotalQuantityPort;

    @InjectMocks
    TotalQuantityService totalQuantityService;

    @Test
    void 세션_총량_계획을_생성한다() {
        // given
        Long sessionalPlanId = 1L;
        Long userId = 2L;
        Subject subject = new Subject("수학", "과목");
        TotalQuantityAmount totalQuantityAmount = TotalQuantityAmount.of(100, "문제", null);
        CreateTotalQuantityCommand command = new CreateTotalQuantityCommand(
                sessionalPlanId,
                userId,
                subject,
                totalQuantityAmount
        );
        SessionalPlan sessionalPlan = SessionalPlan.create(
                sessionalPlanId,
                userId,
                "세션",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );

        given(loadSessionalPlanPort.loadBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.of(sessionalPlan));
        given(saveTotalQuantityPort.save(any(TotalQuantityPlan.class)))
                .willReturn(10L);

        // when
        Long result = totalQuantityService.create(command);

        // then
        assertThat(result).isEqualTo(10L);

        ArgumentCaptor<TotalQuantityPlan> totalQuantityPlanCaptor = ArgumentCaptor.forClass(TotalQuantityPlan.class);
        then(loadSessionalPlanPort).should().loadBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(saveTotalQuantityPort).should().save(totalQuantityPlanCaptor.capture());

        TotalQuantityPlan totalQuantityPlan = totalQuantityPlanCaptor.getValue();
        assertThat(totalQuantityPlan.totalQuantity().getSessionalPlan()).isEqualTo(sessionalPlan);
        assertThat(totalQuantityPlan.totalQuantity().getSubject()).isEqualTo(subject);
        assertThat(totalQuantityPlan.totalQuantity().getTotalQuantityAmount()).isEqualTo(totalQuantityAmount);
        assertThat(totalQuantityPlan.weeklyQuantities()).hasSize(2);
    }

    @Test
    void 세셔널_플랜이_없으면_세션_총량_계획_생성에_실패한다() {
        // given
        Long sessionalPlanId = 1L;
        Long userId = 2L;
        CreateTotalQuantityCommand command = CreateTotalQuantityCommand.of(
                sessionalPlanId,
                userId,
                "수학",
                "과목",
                100,
                "문제",
                null
        );

        given(loadSessionalPlanPort.loadBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> totalQuantityService.create(command))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(loadSessionalPlanPort).should().loadBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(saveTotalQuantityPort).should(never()).save(any(TotalQuantityPlan.class));
    }
}