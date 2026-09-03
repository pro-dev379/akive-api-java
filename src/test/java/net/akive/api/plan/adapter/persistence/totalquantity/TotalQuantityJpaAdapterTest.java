package net.akive.api.plan.adapter.persistence.totalquantity;

import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.akive.api.common.exception.business.NotFoundError.TOTAL_QUANTITY_NOT_FOUND;
import static net.akive.api.plan.domain.PlanFixture.createTotalQuantity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TotalQuantityJpaAdapterTest extends RepositoryTestSupport {

    @Test
    void 세션_총량을_저장한다() {
        SessionalPlan savedSessionalPlan = saveSessionalPlan();

        TotalQuantity savedTotalQuantity = totalQuantityRepository.save(createTotalQuantity(savedSessionalPlan));
        flushAndClear();

        TotalQuantity foundTotalQuantity = totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                savedSessionalPlan.getSessionalPlanId(),
                savedSessionalPlan.getUser().getUserId(),
                savedTotalQuantity.getTotalQuantityId()
        ).orElseThrow();

        assertThat(foundTotalQuantity.getTotalQuantityId()).isEqualTo(savedTotalQuantity.getTotalQuantityId());
        assertThat(foundTotalQuantity.getSessionalPlan().getSessionalPlanId())
                .isEqualTo(savedSessionalPlan.getSessionalPlanId());
        assertThat(foundTotalQuantity.getSubject().name()).isEqualTo("수학");
        assertThat(foundTotalQuantity.getTotalQuantityAmount().quantityAmount().value()).isEqualTo(100);
        assertThat(foundTotalQuantity.getWeeklyQuantities())
                .hasSize(savedSessionalPlan.getSessionPeriod().splitIntoWeeks().size());
    }

    @Test
    void 세셔널_플랜_식별자로_세션_총량을_식별자_오름차순으로_조회한다() {
        SessionalPlan savedSessionalPlan = saveSessionalPlan();
        TotalQuantity firstTotalQuantity = totalQuantityRepository.save(
                createTotalQuantity(savedSessionalPlan, "수학")
        );
        TotalQuantity secondTotalQuantity = totalQuantityRepository.save(
                createTotalQuantity(savedSessionalPlan, "영어")
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
        TotalQuantity savedTotalQuantity = saveTotalQuantity();
        Long sessionalPlanId = savedTotalQuantity.getSessionalPlan().getSessionalPlanId();
        flushAndClear();

        assertThat(totalQuantityRepository.existsBySessionalPlanId(sessionalPlanId)).isTrue();
        assertThat(totalQuantityRepository.existsBySessionalPlanIdAndSubjectName(sessionalPlanId, "수학")).isTrue();
        assertThat(totalQuantityRepository.countBySessionalPlanId(sessionalPlanId)).isEqualTo(1);
    }

    @Test
    void 자기_자신을_제외하고_같은_과목명의_세션_총량이_있는지_조회한다() {
        SessionalPlan savedSessionalPlan = saveSessionalPlan();
        TotalQuantity firstTotalQuantity = totalQuantityRepository.save(
                createTotalQuantity(savedSessionalPlan, "수학")
        );
        TotalQuantity secondTotalQuantity = totalQuantityRepository.save(
                createTotalQuantity(savedSessionalPlan, "영어")
        );
        Long sessionalPlanId = savedSessionalPlan.getSessionalPlanId();
        flushAndClear();

        assertThat(totalQuantityRepository.existsBySessionalPlanIdAndSubjectNameAndTotalQuantityIdNot(
                sessionalPlanId,
                "수학",
                firstTotalQuantity.getTotalQuantityId()
        )).isFalse();
        assertThat(totalQuantityRepository.existsBySessionalPlanIdAndSubjectNameAndTotalQuantityIdNot(
                sessionalPlanId,
                "수학",
                secondTotalQuantity.getTotalQuantityId()
        )).isTrue();
    }

    @Test
    void 세션_총량을_삭제하면_주간_계획량과_일일_계획량도_모두_삭제된다() {
        TotalQuantity savedTotalQuantity = saveTotalQuantity();
        Long sessionalPlanId = savedTotalQuantity.getSessionalPlan().getSessionalPlanId();
        Long userId = savedTotalQuantity.getSessionalPlan().getUser().getUserId();
        Long totalQuantityId = savedTotalQuantity.getTotalQuantityId();
        flushAndClear();

        assertThat(countWeeklyQuantitiesByTotalQuantityId(totalQuantityId)).isPositive();
        assertThat(countDailyQuantitiesByTotalQuantityId(totalQuantityId)).isPositive();

        totalQuantityRepository.deleteBySessionalPlanIdAndUserIdAndTotalQuantityId(
                sessionalPlanId,
                userId,
                totalQuantityId
        );
        flushAndClear();

        assertThat(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                sessionalPlanId,
                userId,
                totalQuantityId
        )).isEmpty();
        assertThat(totalQuantityRepository.countBySessionalPlanId(sessionalPlanId)).isZero();
        assertThat(countWeeklyQuantitiesByTotalQuantityId(totalQuantityId)).isZero();
        assertThat(countDailyQuantitiesByTotalQuantityId(totalQuantityId)).isZero();
    }

    @Test
    void 세션_총량이_없으면_삭제에_실패하고_예외를_던진다() {
        TotalQuantity savedTotalQuantity = saveTotalQuantity();
        Long sessionalPlanId = savedTotalQuantity.getSessionalPlan().getSessionalPlanId();
        Long userId = savedTotalQuantity.getSessionalPlan().getUser().getUserId();
        Long totalQuantityId = savedTotalQuantity.getTotalQuantityId();
        flushAndClear();

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityRepository.deleteBySessionalPlanIdAndUserIdAndTotalQuantityId(
                        sessionalPlanId,
                        userId,
                        Long.MAX_VALUE
                ))
                .extracting(NotFoundException::getError)
                .isEqualTo(TOTAL_QUANTITY_NOT_FOUND);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityRepository.deleteBySessionalPlanIdAndUserIdAndTotalQuantityId(
                        sessionalPlanId,
                        Long.MAX_VALUE,
                        totalQuantityId
                ))
                .extracting(NotFoundException::getError)
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
