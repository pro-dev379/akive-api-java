package net.akive.api.mentoring.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MentoringProfileTest {

    @Test
    void 기본_멘토링_프로필을_생성한다() {
        // given
        Long userId = 1L;

        // when
        MentoringProfile mentoringProfile = MentoringProfile.createDefault(userId);

        // then
        assertThat(mentoringProfile.getUserId()).isEqualTo(userId);
    }
}