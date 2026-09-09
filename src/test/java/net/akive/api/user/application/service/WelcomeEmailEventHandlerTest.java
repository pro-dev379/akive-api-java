package net.akive.api.user.application.service;

import net.akive.api.user.application.event.UserSignedUpEvent;
import net.akive.api.user.application.required.WelcomeEmailSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.user.domain.UserDetailFixture.NICKNAME;
import static net.akive.api.user.domain.UserFixture.EMAIL;
import static net.akive.api.user.domain.UserFixture.USER_ID;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class WelcomeEmailEventHandlerTest {

    @Mock
    WelcomeEmailSender welcomeEmailSender;

    @InjectMocks
    WelcomeEmailEventHandler welcomeEmailEventHandler;

    @Test
    void 회원_가입_이벤트를_처리해서_웰컴_메일을_발송한다() {
        UserSignedUpEvent event = new UserSignedUpEvent(USER_ID, EMAIL, NICKNAME);

        welcomeEmailEventHandler.handle(event);

        then(welcomeEmailSender).should().send(EMAIL, NICKNAME);
    }
}
