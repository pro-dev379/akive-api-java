package net.akive.api.auth.adapter.emailverification.delivery;

import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.email.application.provided.dto.EmailSendCommand;
import net.akive.api.email.application.provided.EmailSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static net.akive.api.auth.domain.emailverification.EmailVerificationFixture.createEmailVerification;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFICATION_CODE_EXPIRATION_DURATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class EmailVerificationSenderAdapterTest {

    @Mock
    EmailSender emailSender;

    @InjectMocks
    EmailVerificationSenderAdapter emailVerificationSenderAdapter;

    @Test
    void 이메일_인증번호를_전송한다() {
        EmailVerification emailVerification = createEmailVerification();
        ReflectionTestUtils.setField(emailVerificationSenderAdapter, "from", "from@test.com");

        emailVerificationSenderAdapter.send(emailVerification, VERIFICATION_CODE_EXPIRATION_DURATION);

        ArgumentCaptor<EmailSendCommand> captor = ArgumentCaptor.forClass(EmailSendCommand.class);

        then(emailSender).should().send(captor.capture());

        EmailSendCommand command = captor.getValue();

        assertThat(command.from()).isEqualTo("from@test.com");
        assertThat(command.to()).containsExactly(emailVerification.getEmail().value());
        assertThat(command.subject()).isEqualTo("[AKIVE] 인증 코드가 도착했어요.");
        assertThat(command.templatePath()).isEqualTo("email/verification-code");
        assertThat(command.variables())
                .containsEntry("verificationCode", emailVerification.getVerificationCode().value())
                .containsEntry("expirationMinutes", VERIFICATION_CODE_EXPIRATION_DURATION.toMinutes());
    }
}