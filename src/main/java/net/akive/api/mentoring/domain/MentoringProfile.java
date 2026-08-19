package net.akive.api.mentoring.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.user.domain.User;

import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class MentoringProfile {

    private Long mentoringProfileId;

    private User user;

    private MentoringProfileDetail detail;

    public static MentoringProfile reconstruct(Long mentoringProfileId, User user, MentoringProfileDetail detail) {
        return MentoringProfile.builder()
                .mentoringProfileId(requirePositive(mentoringProfileId))
                .user(requireNonNull(user))
                .detail(detail)
                .build();
    }

    public static MentoringProfile create(User user, MentoringProfileDetail detail) {
        return MentoringProfile.builder()
                .user(requireNonNull(user))
                .detail(detail)
                .build();
    }

    public static MentoringProfile create(User user) {
        return MentoringProfile.builder()
                .user(requireNonNull(user))
                .build();
    }

    public void update(MentoringProfileUpdateInfo mentoringProfileUpdateInfo) {
        this.detail = new MentoringProfileDetail(
                mentoringProfileUpdateInfo.goal(),
                mentoringProfileUpdateInfo.preference(),
                mentoringProfileUpdateInfo.constraint(),
                mentoringProfileUpdateInfo.weakness(),
                mentoringProfileUpdateInfo.strength(),
                mentoringProfileUpdateInfo.recurringPattern(),
                mentoringProfileUpdateInfo.decisionOrCommitment(),
                mentoringProfileUpdateInfo.studyPlan(),
                mentoringProfileUpdateInfo.behavioralPattern(),
                mentoringProfileUpdateInfo.effectiveAdvice()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MentoringProfile that = (MentoringProfile) o;

        return this.mentoringProfileId != null
                && Objects.equals(this.mentoringProfileId, that.mentoringProfileId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
