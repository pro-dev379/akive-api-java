package net.akive.api.mentoring.adapter.persistence;

import net.akive.api.mentoring.adapter.persistence.entity.MentoringProfileEntity;
import net.akive.api.mentoring.domain.MentoringProfile;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import static net.akive.api.user.domain.fixture.UserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;

class MentoringProfileJpaAdapterTest extends RepositoryTestSupport {

    @Test
    void 멘토링_프로필을_저장한다() {
        User savedUser = userRepository.save(createUser());
        MentoringProfile mentoringProfile = MentoringProfile.create(savedUser);

        MentoringProfile savedMentoringProfile = mentoringProfileRepository.save(mentoringProfile);
        flushAndClear();

        MentoringProfileEntity foundMentoringProfileEntity = entityManager.find(
                MentoringProfileEntity.class,
                savedMentoringProfile.getMentoringProfileId()
        );
        MentoringProfile foundMentoringProfile = foundMentoringProfileEntity.to();

        assertThat(foundMentoringProfile.getMentoringProfileId())
                .isEqualTo(savedMentoringProfile.getMentoringProfileId());
        assertThat(foundMentoringProfile.getUser()).isEqualTo(savedUser);
    }
}
