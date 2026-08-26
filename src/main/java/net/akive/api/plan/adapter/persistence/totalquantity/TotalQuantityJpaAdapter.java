package net.akive.api.plan.adapter.persistence.totalquantity;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.adapter.persistence.entity.TotalQuantityEntity;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import net.akive.api.plan.domain.model.TotalQuantity;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static net.akive.api.common.exception.business.NotFoundError.TOTAL_QUANTITY_NOT_FOUND;
import static org.springframework.data.domain.Sort.Direction.ASC;

@Repository
@RequiredArgsConstructor
class TotalQuantityJpaAdapter implements TotalQuantityRepository {

    private final TotalQuantityJpaRepository totalQuantityJpaRepository;

    @Override
    public TotalQuantity save(TotalQuantity totalQuantity) {
        TotalQuantityEntity totalQuantityEntity = TotalQuantityEntity.from(totalQuantity);

        return totalQuantityJpaRepository.save(totalQuantityEntity).to();
    }

    @Override
    public List<TotalQuantity> findAllBySessionalPlanIdOrderByTotalQuantityIdAsc(Long sessionalPlanId) {
        return totalQuantityJpaRepository
                .findBySessionalPlan_SessionalPlanId(sessionalPlanId, Sort.by(ASC, "totalQuantityId"))
                .stream()
                .map(TotalQuantityEntity::to)
                .toList();
    }

    @Override
    public Optional<TotalQuantity> findBySessionalPlanIdAndTotalQuantityId(
            Long sessionalPlanId,
            Long totalQuantityId
    ) {
        return totalQuantityJpaRepository
                .findBySessionalPlan_SessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId)
                .map(TotalQuantityEntity::to);
    }

    @Override
    public boolean existsBySessionalPlanId(Long sessionalPlanId) {
        return totalQuantityJpaRepository.existsBySessionalPlan_SessionalPlanId(sessionalPlanId);
    }

    @Override
    public boolean existsBySessionalPlanIdAndSubjectName(Long sessionalPlanId, String subjectName) {
        return totalQuantityJpaRepository.existsBySessionalPlan_SessionalPlanIdAndSubject_Name(
                sessionalPlanId,
                subjectName
        );
    }

    @Override
    public void deleteByTotalQuantityIdAndSessionalPlanId(Long totalQuantityId, Long sessionalPlanId) {
        TotalQuantityEntity totalQuantityEntity = totalQuantityJpaRepository
                .findByTotalQuantityIdAndSessionalPlan_SessionalPlanId(totalQuantityId, sessionalPlanId)
                .orElseThrow(() -> new NotFoundException(TOTAL_QUANTITY_NOT_FOUND));

        totalQuantityJpaRepository.delete(totalQuantityEntity);
    }

    @Override
    public long countBySessionalPlanId(Long sessionalPlanId) {
        return totalQuantityJpaRepository.countBySessionalPlan_SessionalPlanId(sessionalPlanId);
    }
}
