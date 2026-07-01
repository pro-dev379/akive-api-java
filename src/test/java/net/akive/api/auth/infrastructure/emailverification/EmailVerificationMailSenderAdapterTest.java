package net.akive.api.auth.infrastructure.emailverification;

import net.akive.api.common.email.EmailSenderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class EmailVerificationMailSenderAdapterTest {

    @Mock
    EmailSenderService emailSenderService;

    @InjectMocks
    EmailVerificationMailSenderAdapter emailVerificationMailSenderAdapter;

    @Test
    void 이메일_인증_메일을_발송한다() {
        // given
        String email = "test@test.com";
        String verificationCode = "123456";
        Duration expireDuration = Duration.ofMinutes(5);

        // when
        emailVerificationMailSenderAdapter.send(
                email,
                verificationCode,
                expireDuration
        );

        // then
        then(emailSenderService).should().send(
                eq(List.of(email)),
                eq("[AKIVE] 인증 코드가 도착했어요."),
                eq("email/verification-code"),
                argThat(variables ->
                        variables.get("verificationCode").equals(verificationCode)
                                && variables.get("expireMin").equals(5L)
                )
        );
    }
}