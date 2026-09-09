package net.akive.api.user.adapter.webapi;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.support.PrivateApiTestSupport;
import net.akive.api.user.application.provided.UserAccountManager;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.user.domain.fixture.UserDetailFixture.NICKNAME;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@WebMvcTest(MemberAccountPrivateApi.class)
class MemberAccountPrivateApiTest extends PrivateApiTestSupport {

    private static final String MEMBER_ME_URL = "/members/me";

    @MockitoBean
    UserAccountManager userAccountManager;

    @MockitoBean
    UserFinder userFinder;

    @Test
    void 개인_정보를_조회한다() {
        User user = createReconstructedUser(USER_ID);
        given(userFinder.findOrThrowByUserIdAndNotWithdrawn(USER_ID)).willReturn(user);

        MvcTestResultAssert result = get(MEMBER_ME_URL, USER_ID).hasStatus(OK);

        result.bodyJson().extractingPath("$.email").isEqualTo(EMAIL.value());
        result.bodyJson().extractingPath("$.nickname").isEqualTo(NICKNAME);
        result.bodyJson().extractingPath("$.roleType").isEqualTo(ROLE_TYPE.name());
        result.bodyJson().extractingPath("$.loginType").isEqualTo(LOGIN_TYPE.name());
        result.bodyJson().extractingPath("$.aim").isNull();
        result.bodyJson().extractingPath("$.imgUrl").isNull();

        then(userFinder).should().findOrThrowByUserIdAndNotWithdrawn(USER_ID);
    }

    @Test
    void 개인_정보_조회_시_멤버를_조회할_수_없으면_404를_응답한다() {
        given(userFinder.findOrThrowByUserIdAndNotWithdrawn(UNKNOWN_USER_ID))
                .willThrow(new ResourceNotFoundException(USER_NOT_FOUND));

        MvcTestResultAssert result = get(MEMBER_ME_URL, UNKNOWN_USER_ID).hasStatus(NOT_FOUND);

        assertErrorResponse(result, USER_NOT_FOUND);
        then(userFinder).should().findOrThrowByUserIdAndNotWithdrawn(UNKNOWN_USER_ID);
    }
}
