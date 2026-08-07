package net.akive.api.plan.application.service;

import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;

import net.akive.api.plan.application.required.LoadSessionalPlanPort;
import net.akive.api.plan.domain.SessionPeriod;
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
import java.util.Optional;

import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.ConflictError.SUBJECT_NAME_ALREADY_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TotalQuantityDomainServiceTest {

    private static final Long SESSIONAL_PLAN_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Subject SUBJECT = new Subject("수학", "과목");
    private static final TotalQuantityAmount TOTAL_QUANTITY_AMOUNT = TotalQuantityAmount.of(100, "문제", null);

    @Mock
    LoadSessionalPlanPort loadSessionalPlanPort;

    @Mock
    TotalQuantityValidator totalQuantityValidator;

    @InjectMocks
    TotalQuantityDomainService totalQuantityDomainService;

    @Test
    void 생성_가능_검증_후_세션_총량_계획을_생성한다() {
        // given
        SessionalPlan sessionalPlan = sessionalPlan();

        given(loadSessionalPlanPort.loadBySessionalPlanIdAndUserId(SESSIONAL_PLAN_ID, USER_ID))
                .willReturn(Optional.of(sessionalPlan));

        // when
        TotalQuantityPlan totalQuantityPlan = totalQuantityDomainService.create(
                SESSIONAL_PLAN_ID,
                USER_ID,
                SUBJECT,
                TOTAL_QUANTITY_AMOUNT
        );

        // then
        assertThat(totalQuantityPlan.totalQuantity().getSessionalPlan()).isEqualTo(sessionalPlan);
        assertThat(totalQuantityPlan.totalQuantity().getSubject()).isEqualTo(SUBJECT);
        assertThat(totalQuantityPlan.totalQuantity().getTotalQuantityAmount()).isEqualTo(TOTAL_QUANTITY_AMOUNT);
        assertThat(totalQuantityPlan.weeklyQuantities()).hasSize(2);

        then(loadSessionalPlanPort).should().loadBySessionalPlanIdAndUserId(SESSIONAL_PLAN_ID, USER_ID);
        then(totalQuantityValidator).should().validateCreatable(SESSIONAL_PLAN_ID, SUBJECT.name());
    }

    @Test
    void 세셔널_플랜이_없으면_세션_총량_계획_생성에_실패한다() {
        // given
        given(loadSessionalPlanPort.loadBySessionalPlanIdAndUserId(SESSIONAL_PLAN_ID, USER_ID))
                .willReturn(Optional.empty());

        // when & then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityDomainService.create(
                        SESSIONAL_PLAN_ID,
                        USER_ID,
                        SUBJECT,
                        TOTAL_QUANTITY_AMOUNT
                ))
                .extracting(NotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(loadSessionalPlanPort).should().loadBySessionalPlanIdAndUserId(SESSIONAL_PLAN_ID, USER_ID);
        then(totalQuantityValidator).should(never()).validateCreatable(anyLong(), anyString());
    }

    @Test
    void 생성_가능_검증이_실패하면_세션_총량_계획_생성에_실패한다() {
        // given
        given(loadSessionalPlanPort.loadBySessionalPlanIdAndUserId(SESSIONAL_PLAN_ID, USER_ID))
                .willReturn(Optional.of(sessionalPlan()));
        willThrow(new ConflictException(SUBJECT_NAME_ALREADY_EXISTS))
                .given(totalQuantityValidator)
                .validateCreatable(SESSIONAL_PLAN_ID, SUBJECT.name());

        // when & then
        assertThatExceptionOfType(ConflictException.class)
                .isThrownBy(() -> totalQuantityDomainService.create(
                        SESSIONAL_PLAN_ID,
                        USER_ID,
                        SUBJECT,
                        TOTAL_QUANTITY_AMOUNT
                ))
                .extracting(ConflictException::getError)
                .isEqualTo(SUBJECT_NAME_ALREADY_EXISTS);

        then(loadSessionalPlanPort).should().loadBySessionalPlanIdAndUserId(SESSIONAL_PLAN_ID, USER_ID);
        then(totalQuantityValidator).should().validateCreatable(SESSIONAL_PLAN_ID, SUBJECT.name());
    }

    private SessionalPlan sessionalPlan() {
        return SessionalPlan.create(
                SESSIONAL_PLAN_ID,
                USER_ID,
                "세션",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );
    }
}
