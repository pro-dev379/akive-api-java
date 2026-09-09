package net.akive.api.user.adapter.persistence.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.user.domain.Withdrawal;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Embeddable
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class WithdrawalEntity {

    private LocalDateTime withdrawnAt;

    private String withdrawalReason;

    public static WithdrawalEntity from(Withdrawal withdrawal) {
        return new WithdrawalEntity(withdrawal.withdrawnAt(), withdrawal.withdrawalReason());
    }

    public Withdrawal to() {
        return new Withdrawal(this.withdrawnAt, this.withdrawalReason);
    }
}
