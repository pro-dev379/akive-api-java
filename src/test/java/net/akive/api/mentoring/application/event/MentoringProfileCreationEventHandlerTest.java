package net.akive.api.mentoring.application.event;

import net.akive.api.mentoring.application.provided.MentoringProfileCreator;
import net.akive.api.user.application.event.UserSignedUpEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.user.domain.fixture.UserDetailFixture.NICKNAME;
import static net.akive.api.user.domain.fixture.UserFixture.EMAIL;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class MentoringProfileCreationEventHandlerTest {

    @Mock
    MentoringProfileCreator mentoringProfileCreator;

    @InjectMocks
    MentoringProfileCreationEventHandler mentoringProfileCreationEventHandler;

    @Test
    void 회원_가입_이벤트를_받으면_기본_멘토링_프로필을_생성한다() {
        UserSignedUpEvent userSignedUpEvent = new UserSignedUpEvent(USER_ID, EMAIL, NICKNAME);

        mentoringProfileCreationEventHandler.handle(userSignedUpEvent);

        then(mentoringProfileCreator).should().create(USER_ID);
    }
}
