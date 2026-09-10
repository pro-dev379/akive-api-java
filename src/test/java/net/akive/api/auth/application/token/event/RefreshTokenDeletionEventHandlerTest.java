package net.akive.api.auth.application.token.event;

import net.akive.api.auth.application.token.provided.RefreshTokenSessionManager;
import net.akive.api.user.application.event.UserWithdrawnEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RefreshTokenDeletionEventHandlerTest {

    @Mock
    RefreshTokenSessionManager refreshTokenSessionManager;

    @InjectMocks
    RefreshTokenDeletionEventHandler refreshTokenDeletionEventHandler;

    @Test
    void 회원_탈퇴_이벤트를_받으면_해당_회원의_리프레시_토큰_세션을_무효화_한다() {
        UserWithdrawnEvent userWithdrawnEvent = new UserWithdrawnEvent(USER_ID);

        refreshTokenDeletionEventHandler.handle(userWithdrawnEvent);

        then(refreshTokenSessionManager).should().revokeByUserId(USER_ID);
    }
}
