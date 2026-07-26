package net.akive.api.plan.application.service;

import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.dto.command.CreateTotalQuantityCommand;
import net.akive.api.plan.application.dto.command.DeleteTotalQuantityCommand;
import net.akive.api.plan.application.dto.query.GetSessionalPlanTotalQuantityQuery;
import net.akive.api.plan.application.dto.result.sessionalplan.GetSessionalPlanResult;
import net.akive.api.plan.application.port.out.DeleteTotalQuantityPort;
import net.akive.api.plan.application.port.out.LoadSessionalPlanPort;
import net.akive.api.plan.application.port.out.LoadTotalQuantityPort;
import net.akive.api.plan.application.port.out.SaveTotalQuantityPort;
import net.akive.api.plan.application.port.out.dto.SessionalPlanTotalQuantityData;
import net.akive.api.plan.application.port.out.dto.SessionalPlanWeeklyQuantityAmountData;
import net.akive.api.plan.application.port.out.dto.SessionalPlanWeeklyQuantityData;
import net.akive.api.plan.domain.SessionPeriod;
import net.akive.api.plan.domain.SessionWeek;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.domain.Subject;
import net.akive.api.plan.domain.TotalQuantityAmount;
import net.akive.api.plan.domain.TotalQuantityPlan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_TOTAL_QUANTITY_NOT_FOUND;
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
    LoadTotalQuantityPort loadTotalQuantityPort;

    @Mock
    TotalQuantityDomainService totalQuantityDomainService;

    @Mock
    SaveTotalQuantityPort saveTotalQuantityPort;

    @Mock
    DeleteTotalQuantityPort deleteTotalQuantityPort;

    @Mock
    TotalQuantityPlan totalQuantityPlan;

    @InjectMocks
    TotalQuantityService totalQuantityService;

    @Test
    void 세션_총량을_조회한다() {
        // given
        Long sessionalPlanId = 1L;
        Long userId = 2L;
        Long totalQuantityId = 10L;
        GetSessionalPlanTotalQuantityQuery query = new GetSessionalPlanTotalQuantityQuery(
                sessionalPlanId,
                userId,
                totalQuantityId
        );
        SessionalPlan sessionalPlan = SessionalPlan.create(
                sessionalPlanId,
                userId,
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );
        List<SessionWeek> sessionWeeks = sessionalPlan.getSessionPeriod().splitIntoWeeks();
        SessionalPlanTotalQuantityData data = new SessionalPlanTotalQuantityData(
                totalQuantityId,
                sessionalPlanId,
                new Subject("수학", "과목"),
                TotalQuantityAmount.of(100, "문제", "총량 메모"),
                List.of(
                        new SessionalPlanWeeklyQuantityData(
                                100L,
                                totalQuantityId,
                                sessionWeeks.getFirst(),
                                new SessionalPlanWeeklyQuantityAmountData(100, 20, 80, "주차 메모")
                        )
                )
        );

        given(loadSessionalPlanPort.loadBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.of(sessionalPlan));
        given(loadTotalQuantityPort.loadSessionalPlanTotalQuantityBySessionalPlanIdAndTotalQuantityId(
                sessionalPlanId,
                totalQuantityId,
                sessionWeeks
        )).willReturn(Optional.of(data));

        // when
        GetSessionalPlanResult.TotalQuantityResult result = totalQuantityService.get(query);

        // then
        assertThat(result.totalQuantityId()).isEqualTo(totalQuantityId);
        assertThat(result.subject().name()).isEqualTo("수학");
        assertThat(result.subject().category()).isEqualTo("과목");
        assertThat(result.totalQuantityAmount().quantityAmount()).isEqualTo(100);
        assertThat(result.totalQuantityAmount().unit()).isEqualTo("문제");
        assertThat(result.totalQuantityAmount().quantityMemo()).isEqualTo("총량 메모");
        assertThat(result.weeklyQuantities()).hasSize(1);
        assertThat(result.weeklyQuantities().getFirst().weeklyQuantityId()).isEqualTo(100L);
        assertThat(result.weeklyQuantities().getFirst().weeklyQuantityAmount().quantityAmount()).isEqualTo(100);
        assertThat(result.weeklyQuantities().getFirst().weeklyQuantityAmount().completedQuantityAmount()).isEqualTo(20);
        assertThat(result.weeklyQuantities().getFirst().weeklyQuantityAmount().delayedQuantityAmount()).isEqualTo(80);

        then(loadSessionalPlanPort).should().loadBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(loadTotalQuantityPort).should().loadSessionalPlanTotalQuantityBySessionalPlanIdAndTotalQuantityId(
                sessionalPlanId,
                totalQuantityId,
                sessionWeeks
        );
    }

    @Test
    void 세셔널_플랜이_없으면_세션_총량_조회에_실패한다() {
        // given
        Long sessionalPlanId = 1L;
        Long userId = 2L;
        Long totalQuantityId = 10L;
        GetSessionalPlanTotalQuantityQuery query = new GetSessionalPlanTotalQuantityQuery(
                sessionalPlanId,
                userId,
                totalQuantityId
        );

        given(loadSessionalPlanPort.loadBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityService.get(query))
                .extracting(NotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(loadSessionalPlanPort).should().loadBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(loadTotalQuantityPort).should(never())
                .loadSessionalPlanTotalQuantityBySessionalPlanIdAndTotalQuantityId(any(), any(), any());
    }

    @Test
    void 세션_총량이_없으면_세션_총량_조회에_실패한다() {
        // given
        Long sessionalPlanId = 1L;
        Long userId = 2L;
        Long totalQuantityId = 10L;
        GetSessionalPlanTotalQuantityQuery query = new GetSessionalPlanTotalQuantityQuery(
                sessionalPlanId,
                userId,
                totalQuantityId
        );
        SessionalPlan sessionalPlan = SessionalPlan.create(
                sessionalPlanId,
                userId,
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );
        List<SessionWeek> sessionWeeks = sessionalPlan.getSessionPeriod().splitIntoWeeks();

        given(loadSessionalPlanPort.loadBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.of(sessionalPlan));
        given(loadTotalQuantityPort.loadSessionalPlanTotalQuantityBySessionalPlanIdAndTotalQuantityId(
                sessionalPlanId,
                totalQuantityId,
                sessionWeeks
        )).willReturn(Optional.empty());

        // when & then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityService.get(query))
                .extracting(NotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_TOTAL_QUANTITY_NOT_FOUND);

        then(loadSessionalPlanPort).should().loadBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(loadTotalQuantityPort).should().loadSessionalPlanTotalQuantityBySessionalPlanIdAndTotalQuantityId(
                sessionalPlanId,
                totalQuantityId,
                sessionWeeks
        );
    }

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

    @Test
    void 세션_총량을_삭제한다() {
        // given
        Long sessionalPlanId = 1L;
        Long userId = 2L;
        Long totalQuantityId = 10L;
        DeleteTotalQuantityCommand command = new DeleteTotalQuantityCommand(
                sessionalPlanId,
                userId,
                totalQuantityId
        );

        given(deleteTotalQuantityPort.deleteByTotalQuantityIdAndSessionalPlanIdAndUserId(
                totalQuantityId,
                sessionalPlanId,
                userId
        )).willReturn(1L);

        // when
        totalQuantityService.delete(command);

        // then
        then(deleteTotalQuantityPort).should().deleteByTotalQuantityIdAndSessionalPlanIdAndUserId(
                totalQuantityId,
                sessionalPlanId,
                userId
        );
    }

    @Test
    void 삭제할_세션_총량이_없으면_세션_총량_삭제에_실패한다() {
        // given
        Long sessionalPlanId = 1L;
        Long userId = 2L;
        Long totalQuantityId = 10L;
        DeleteTotalQuantityCommand command = new DeleteTotalQuantityCommand(
                sessionalPlanId,
                userId,
                totalQuantityId
        );

        given(deleteTotalQuantityPort.deleteByTotalQuantityIdAndSessionalPlanIdAndUserId(
                totalQuantityId,
                sessionalPlanId,
                userId
        )).willReturn(0L);

        // when & then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityService.delete(command))
                .extracting(NotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_TOTAL_QUANTITY_NOT_FOUND);

        then(deleteTotalQuantityPort).should().deleteByTotalQuantityIdAndSessionalPlanIdAndUserId(
                totalQuantityId,
                sessionalPlanId,
                userId
        );
    }
}
