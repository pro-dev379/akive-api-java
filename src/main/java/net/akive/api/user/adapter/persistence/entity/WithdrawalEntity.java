package net.akive.api.user.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.user.domain.Withdrawal;
import net.akive.api.user.domain.WithdrawalReason;

import java.time.Instant;

import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Embeddable
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class WithdrawalEntity {

    private Instant withdrawnAt;

    @Enumerated(STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    private WithdrawalReason withdrawalReason;

    public static WithdrawalEntity from(Withdrawal withdrawal) {
        return new WithdrawalEntity(withdrawal.withdrawnAt(), withdrawal.withdrawalReason());
    }

    public Withdrawal to() {
        return new Withdrawal(this.withdrawnAt, this.withdrawalReason);
    }
}
