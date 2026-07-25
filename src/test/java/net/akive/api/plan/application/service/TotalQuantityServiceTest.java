package net.akive.api.plan.application.service;

import net.akive.api.common.exception.business.NotFoundException;

import net.akive.api.plan.application.dto.command.CreateTotalQuantityCommand;
import net.akive.api.plan.application.port.out.SaveTotalQuantityPort;
import net.akive.api.plan.domain.Subject;
import net.akive.api.plan.domain.TotalQuantityAmount;
import net.akive.api.plan.domain.TotalQuantityPlan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TotalQuantityServiceTest {

    @Mock
    TotalQuantityDomainService totalQuantityDomainService;

    @Mock
    SaveTotalQuantityPort saveTotalQuantityPort;

    @Mock
    TotalQuantityPlan totalQuantityPlan;

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

        given(totalQuantityDomainService.create(sessionalPlanId, userId, subject, totalQuantityAmount))
                .willReturn(totalQuantityPlan);
        given(saveTotalQuantityPort.save(totalQuantityPlan))
                .willReturn(10L);

        // when
        Long result = totalQuantityService.create(command);

        // then
        assertThat(result).isEqualTo(10L);

        then(totalQuantityDomainService).should().create(sessionalPlanId, userId, subject, totalQuantityAmount);
        then(saveTotalQuantityPort).should().save(totalQuantityPlan);
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

        given(totalQuantityDomainService.create(
                command.sessionalPlanId(),
                command.userId(),
                command.subject(),
                command.totalQuantityAmount()
        )).willThrow(new NotFoundException(SESSIONAL_PLAN_NOT_FOUND));

        // when & then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityService.create(command))
                .extracting(NotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(totalQuantityDomainService).should().create(
                command.sessionalPlanId(),
                command.userId(),
                command.subject(),
                command.totalQuantityAmount()
        );
        then(saveTotalQuantityPort).should(never()).save(any(TotalQuantityPlan.class));
    }
}