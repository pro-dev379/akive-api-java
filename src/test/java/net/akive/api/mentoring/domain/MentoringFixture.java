package net.akive.api.mentoring.domain;

import static net.akive.api.user.domain.UserFixture.createUser;

public class MentoringFixture {

    public static MentoringProfile createMentoringProfile() {
        return MentoringProfile.create(createUser());
    }

    public static MentoringProfile createMentoringProfileWithDetail() {
        MentoringProfileDetail detail = new MentoringProfileDetail(
                "목표",
                "선호",
                "제약",
                "약점",
                "강점",
                "반복 패턴",
                "결정과 약속",
                "학습 계획",
                "행동 패턴",
                "효과적인 조언"
        );

        return MentoringProfile.create(createUser(), detail);
    }
}
