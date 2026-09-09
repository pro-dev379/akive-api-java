package net.akive.api.plan.adapter.persistence.totalquantity;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.akive.api.common.exception.business.ResourceNotFoundError.TOTAL_QUANTITY_NOT_FOUND;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.UNKNOWN_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TotalQuantityJpaAdapterTest extends RepositoryTestSupport {

    @Test
    void 세션_총량을_저장한다() {
        SessionalPlan savedSessionalPlan = saveSessionalPlan();

        TotalQuantity totalQuantity = TotalQuantity.create(
                savedSessionalPlan,
                createSubject(FIRST_SUBJECT_NAME),
                createTotalQuantityAmount(TOTAL_AMOUNT)
        );
        TotalQuantity savedTotalQuantity = totalQuantityRepository.save(totalQuantity);
        flushAndClear();

        TotalQuantity foundTotalQuantity = totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                savedSessionalPlan.getSessionalPlanId(),
                savedSessionalPlan.getUser().getUserId(),
                savedTotalQuantity.getTotalQuantityId()
        ).orElseThrow();

        assertThat(foundTotalQuantity.getTotalQuantityId()).isEqualTo(savedTotalQuantity.getTotalQuantityId());
        assertThat(foundTotalQuantity.getSessionalPlan().getSessionalPlanId())
                .isEqualTo(savedSessionalPlan.getSessionalPlanId());
        assertThat(foundTotalQuantity.getSubject().name()).isEqualTo(FIRST_SUBJECT_NAME);
        assertThat(foundTotalQuantity.getTotalQuantityAmount().quantityAmount().value()).isEqualTo(TOTAL_AMOUNT);
        assertThat(foundTotalQuantity.getWeeklyQuantities())
                .hasSize(savedSessionalPlan.getSessionPeriod().splitIntoWeeks().size());
    }

    @Test
    void 세셔널_플랜_식별자로_세션_총량을_식별자_오름차순으로_조회한다() {
        SessionalPlan savedSessionalPlan = saveSessionalPlan();
        TotalQuantity firstTotalQuantity = totalQuantityRepository.save(
                createTotalQuantity(savedSessionalPlan, FIRST_SUBJECT_NAME)
        );
        TotalQuantity secondTotalQuantity = totalQuantityRepository.save(
                createTotalQuantity(savedSessionalPlan, SECOND_SUBJECT_NAME)
        );
        flushAndClear();

        List<TotalQuantity> totalQuantities = totalQuantityRepository
                .findAllBySessionalPlanIdOrderByTotalQuantityIdAsc(savedSessionalPlan.getSessionalPlanId());

        assertThat(totalQuantities)
                .extracting(TotalQuantity::getTotalQuantityId)
                .containsExactly(
                        firstTotalQuantity.getTotalQuantityId(),
                        secondTotalQuantity.getTotalQuantityId()
                );
    }

    @Test
    void 세셔널_플랜_식별자와_세션_총량_식별자로_조회한다() {
        TotalQuantity savedTotalQuantity = saveTotalQuantity();
        flushAndClear();

        TotalQuantity foundTotalQuantity = totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                savedTotalQuantity.getSessionalPlan().getSessionalPlanId(),
                savedTotalQuantity.getSessionalPlan().getUser().getUserId(),
                savedTotalQuantity.getTotalQuantityId()
        ).orElseThrow();

        assertThat(foundTotalQuantity.getTotalQuantityId()).isEqualTo(savedTotalQuantity.getTotalQuantityId());
        assertThat(foundTotalQuantity.getSubject()).isEqualTo(savedTotalQuantity.getSubject());
        assertThat(foundTotalQuantity.getWeeklyQuantities())
                .hasSize(savedTotalQuantity.getSessionalPlan().getSessionPeriod().splitIntoWeeks().size());
    }

    @Test
    void 세셔널_플랜_식별자로_세션_총량_존재_여부와_개수를_조회한다() {
        TotalQuantity savedTotalQuantity = saveTotalQuantity(FIRST_SUBJECT_NAME);
        flushAndClear();

        assertThat(totalQuantityRepository.existsBySessionalPlanId(
                savedTotalQuantity.getSessionalPlan().getSessionalPlanId())
        ).isTrue();
        assertThat(totalQuantityRepository.existsBySessionalPlanIdAndSubjectName(
                savedTotalQuantity.getSessionalPlan().getSessionalPlanId(),
                FIRST_SUBJECT_NAME
        )).isTrue();
        assertThat(totalQuantityRepository.countBySessionalPlanId(
                savedTotalQuantity.getSessionalPlan().getSessionalPlanId())
        ).isEqualTo(1);
    }

    @Test
    void 자기_자신을_제외하고_같은_과목명의_세션_총량이_있는지_조회한다() {
        SessionalPlan savedSessionalPlan = saveSessionalPlan();
        TotalQuantity firstTotalQuantity = saveTotalQuantity(savedSessionalPlan, FIRST_SUBJECT_NAME);
        TotalQuantity secondTotalQuantity = saveTotalQuantity(savedSessionalPlan, SECOND_SUBJECT_NAME);
        flushAndClear();

        assertThat(totalQuantityRepository.existsBySessionalPlanIdAndSubjectNameAndTotalQuantityIdNot(
                savedSessionalPlan.getSessionalPlanId(),
                FIRST_SUBJECT_NAME,
                firstTotalQuantity.getTotalQuantityId()
        )).isFalse();
        assertThat(totalQuantityRepository.existsBySessionalPlanIdAndSubjectNameAndTotalQuantityIdNot(
                savedSessionalPlan.getSessionalPlanId(),
                FIRST_SUBJECT_NAME,
                secondTotalQuantity.getTotalQuantityId()
        )).isTrue();
    }

    @Test
    void 세션_총량을_삭제하면_주간_계획량과_일일_계획량도_모두_삭제된다() {
        TotalQuantity savedTotalQuantity = saveTotalQuantity();
        flushAndClear();

        assertThat(countWeeklyQuantitiesByTotalQuantityId(savedTotalQuantity.getTotalQuantityId())).isPositive();
        assertThat(countDailyQuantitiesByTotalQuantityId(savedTotalQuantity.getTotalQuantityId())).isPositive();

        totalQuantityRepository.deleteBySessionalPlanIdAndUserIdAndTotalQuantityId(
                savedTotalQuantity.getSessionalPlan().getSessionalPlanId(),
                savedTotalQuantity.getSessionalPlan().getUser().getUserId(),
                savedTotalQuantity.getTotalQuantityId()
        );
        flushAndClear();

        assertThat(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                savedTotalQuantity.getSessionalPlan().getSessionalPlanId(),
                savedTotalQuantity.getSessionalPlan().getUser().getUserId(),
                savedTotalQuantity.getTotalQuantityId()
        )).isEmpty();
        assertThat(totalQuantityRepository.countBySessionalPlanId(
                savedTotalQuantity.getSessionalPlan().getSessionalPlanId())
        ).isZero();
        assertThat(countWeeklyQuantitiesByTotalQuantityId(savedTotalQuantity.getTotalQuantityId())).isZero();
        assertThat(countDailyQuantitiesByTotalQuantityId(savedTotalQuantity.getTotalQuantityId())).isZero();
    }

    @Test
    void 세션_총량이_없으면_삭제에_실패하고_예외를_던진다() {
        TotalQuantity savedTotalQuantity = saveTotalQuantity();
        flushAndClear();

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantityRepository.deleteBySessionalPlanIdAndUserIdAndTotalQuantityId(
                        savedTotalQuantity.getSessionalPlan().getSessionalPlanId(),
                        savedTotalQuantity.getSessionalPlan().getUser().getUserId(),
                        UNKNOWN_TOTAL_QUANTITY_ID
                ))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(TOTAL_QUANTITY_NOT_FOUND);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantityRepository.deleteBySessionalPlanIdAndUserIdAndTotalQuantityId(
                        savedTotalQuantity.getSessionalPlan().getSessionalPlanId(),
                        UNKNOWN_USER_ID,
                        savedTotalQuantity.getTotalQuantityId()
                ))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(TOTAL_QUANTITY_NOT_FOUND);
    }

    private Long countWeeklyQuantitiesByTotalQuantityId(Long totalQuantityId) {
        return entityManager.createQuery(
                        """
                            select count(wq)
                            from WeeklyQuantityEntity wq
                            where wq.totalQuantity.totalQuantityId = :totalQuantityId
                        """,
                        Long.class
                )
                .setParameter("totalQuantityId", totalQuantityId)
                .getSingleResult();
    }

    private Long countDailyQuantitiesByTotalQuantityId(Long totalQuantityId) {
        return entityManager.createQuery(
                        """
                            select count(dq)
                            from DailyQuantityEntity dq
                            join dq.weeklyQuantity wq
                            where wq.totalQuantity.totalQuantityId = :totalQuantityId
                        """,
                        Long.class
                )
                .setParameter("totalQuantityId", totalQuantityId)
                .getSingleResult();
    }
}
