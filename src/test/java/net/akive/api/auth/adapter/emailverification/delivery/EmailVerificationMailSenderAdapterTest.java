package net.akive.api.auth.adapter.emailverification.delivery;

import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.auth.domain.emailverification.EmailVerificationCode;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.domain.Email;
import net.akive.api.common.email.SendEmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class EmailVerificationMailSenderAdapterTest {

    @Mock
    SendEmailService sendEmailService;

    @InjectMocks
    EmailVerificationMailSenderAdapter emailVerificationMailSenderAdapter;

    @Test
    void 이메일_인증_메일을_발송한다() {
        // given
        String email = "test@test.com";
        EmailVerificationCode verificationCode = new EmailVerificationCode("123456");
        Duration expireDuration = Duration.ofMinutes(5);
        EmailVerification emailVerification = EmailVerification.create(
                new Email(email),
                EmailVerificationPurpose.SIGN_UP,
                verificationCode,
                LocalDateTime.of(2025, 5, 21, 10, 0)
        );

        // when
        emailVerificationMailSenderAdapter.send(
                emailVerification,
                expireDuration
        );

        // then
        then(sendEmailService).should().send(
                eq(List.of(email)),
                eq("[AKIVE] 인증 코드가 도착했어요."),
                eq("email/verification-code"),
                argThat(variables ->
                        variables.get("verificationCode").equals(verificationCode.value())
                                && variables.get("expireMin").equals(5L)
                )
        );
    }
}
