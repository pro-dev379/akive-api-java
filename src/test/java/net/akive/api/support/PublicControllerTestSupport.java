package net.akive.api.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.auth.application.emailverification.provided.SendEmailVerificationCodeUseCase;
import net.akive.api.auth.application.emailverification.provided.VerifyEmailVerificationCodeUseCase;
import net.akive.api.auth.application.login.provided.LoginByEmailUseCase;
import net.akive.api.auth.application.token.provided.IssueTokenUseCase;
import net.akive.api.auth.adapter.emailverification.webapi.EmailVerificationController;
import net.akive.api.auth.adapter.login.webapi.LoginController;
import net.akive.api.auth.adapter.token.webapi.TokenController;
import net.akive.api.user.application.provided.SignUpByEmailUseCase;
import net.akive.api.user.adapter.webapi.SignUpController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
        EmailVerificationController.class,
        SignUpController.class,
        LoginController.class,
        TokenController.class,
})
@AutoConfigureMockMvc(addFilters = false)
public abstract class PublicControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected SendEmailVerificationCodeUseCase sendEmailVerificationCodeUseCase;

    @MockitoBean
    protected VerifyEmailVerificationCodeUseCase verifyEmailVerificationCodeUseCase;

    @MockitoBean
    protected SignUpByEmailUseCase signUpByEmailUseCase;

    @MockitoBean
    protected LoginByEmailUseCase loginByEmailUseCase;

    @MockitoBean
    protected IssueTokenUseCase issueTokenUseCase;
}
