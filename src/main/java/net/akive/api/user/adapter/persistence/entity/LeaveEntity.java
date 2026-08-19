package net.akive.api.user.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.user.domain.Leave;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Embeddable
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class LeaveEntity {

    @Column(nullable = false, columnDefinition = "TINYINT(1) UNSIGNED DEFAULT 0")
    private boolean isLeave;

    @Column(name = "leave_reason")
    private String reason;

    public static LeaveEntity from(Leave leave) {
        return new LeaveEntity(leave.isLeave(), leave.reason());
    }

    public Leave to() {
        return new Leave(this.isLeave, this.reason);
    }
}
