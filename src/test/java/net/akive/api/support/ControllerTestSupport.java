package net.akive.api.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.auth.application.port.in.emailverification.SendEmailVerificationCodeUseCase;
import net.akive.api.auth.application.port.in.emailverification.VerifyEmailVerificationCodeUseCase;
import net.akive.api.auth.application.port.in.login.LoginByEmailUseCase;
import net.akive.api.auth.application.port.in.token.IssueTokenUseCase;
import net.akive.api.auth.presentation.EmailVerificationController;
import net.akive.api.auth.presentation.LoginController;
import net.akive.api.auth.presentation.TokenController;
import net.akive.api.user.application.port.in.SignUpByEmailUseCase;
import net.akive.api.user.presentation.SignUpController;
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
public abstract class ControllerTestSupport {

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
