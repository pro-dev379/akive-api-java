package net.akive.api.mentoring.infrastructure;

import net.akive.api.mentoring.domain.MentoringProfile;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;

class MentoringProfileJpaAdapterTest extends RepositoryTestSupport {

    @Test
    void 멘토링_프로필을_저장한다() {
        // given
        User user = User.signUpByEmail(
                "test@test.com",
                "USER@abcde",
                "password123!",
                USER
        );

        User savedUser = saveUserPort.save(user);
        MentoringProfile mentoringProfile = MentoringProfile.createDefault(savedUser.getUserId());

        // when
        MentoringProfile savedMentoringProfile = saveMentoringProfilePort.save(mentoringProfile);

         // then
        assertThat(savedMentoringProfile.getMentoringProfileId()).isNotNull();
        assertThat(savedMentoringProfile.getUserId()).isEqualTo(savedUser.getUserId());
    }
}