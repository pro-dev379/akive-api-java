package net.akive.api.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.auth.adapter.emailverification.webapi.EmailVerificationApi;
import net.akive.api.auth.adapter.login.webapi.LoginApi;
import net.akive.api.auth.adapter.token.webapi.TokenApi;
import net.akive.api.auth.application.emailverification.provided.EmailVerificationCodeIssuer;
import net.akive.api.auth.application.emailverification.provided.EmailVerificationCodeVerifier;
import net.akive.api.auth.application.login.provided.LoginProcessor;
import net.akive.api.auth.application.token.provided.TokenIssuer;
import net.akive.api.user.adapter.webapi.MemberAccountApi;
import net.akive.api.user.application.provided.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebMvcTest(controllers = {
        EmailVerificationApi.class,
        MemberAccountApi.class,
        LoginApi.class,
        TokenApi.class,
})
@AutoConfigureMockMvc(addFilters = false)
public abstract class PublicApiTestSupport {

    @Autowired
    @Deprecated
    protected MockMvc mockMvc;

    @Autowired
    protected MockMvcTester mvcTester;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected EmailVerificationCodeIssuer emailVerificationCodeIssuer;

    @MockitoBean
    protected EmailVerificationCodeVerifier emailVerificationCodeVerifier;

    @MockitoBean
    protected UserAccountManager userAccountManager;

    @MockitoBean
    protected LoginProcessor loginProcessor;

    @MockitoBean
    protected TokenIssuer tokenIssuer;

    protected MvcTestResultAssert postJson(String url, Object body) throws Exception {
        return postJson(url, objectMapper.writeValueAsString(body));
    }

    protected MvcTestResultAssert postJson(String url, String json) {
        return mvcTester.post()
                .uri(url)
                .contentType(APPLICATION_JSON)
                .content(json)
                .assertThat();
    }
}
