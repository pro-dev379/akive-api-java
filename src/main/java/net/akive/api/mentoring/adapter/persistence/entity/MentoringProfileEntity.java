package net.akive.api.mentoring.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.mentoring.domain.MentoringProfile;
import net.akive.api.user.adapter.persistence.entity.UserEntity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(
        name = "mentoring_profile",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux-mentoring_profile-user_id",
                        columnNames = "user_id"
                )
        }
)
@Getter
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class MentoringProfileEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long mentoringProfileId;

    @OneToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Embedded
    private MentoringProfileDetailEntity detail;

    public static MentoringProfileEntity from(MentoringProfile mentoringProfile) {
        return new MentoringProfileEntity(
                mentoringProfile.getMentoringProfileId(),
                UserEntity.from(mentoringProfile.getUser()),
                MentoringProfileDetailEntity.from(mentoringProfile.getDetail())
        );
    }

    public MentoringProfile to() {
        return MentoringProfile.reconstruct(
                this.mentoringProfileId,
                this.user.to(),
                this.detail != null ? this.detail.to() : null
        );
    }
}
