package net.akive.api.user.adapter.delivery;

import net.akive.api.common.domain.Email;
import net.akive.api.core.config.email.EmailProperties;
import net.akive.api.email.application.provided.EmailSender;
import net.akive.api.email.application.provided.dto.EmailSendCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.common.domain.EmailFixture.createEmail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class WelcomeEmailSenderAdapterTest {

    private static final Email EMAIL = createEmail();
    private static final String NICKNAME = "akive";
    private static final String FROM = "welcome@test.com";
    private static final String SUPPORT_EMAIL = "support@test.com";

    @Mock
    EmailSender emailSender;

    @Mock
    EmailProperties emailProperties;

    @Mock
    EmailProperties.FromProperties fromProperties;

    @InjectMocks
    WelcomeEmailSenderAdapter welcomeEmailSenderAdapter;

    @BeforeEach
    void setUp() {
        given(emailProperties.from()).willReturn(fromProperties);
        given(fromProperties.welcome()).willReturn(FROM);
        given(fromProperties.support()).willReturn(SUPPORT_EMAIL);
    }

    @Test
    void 웰컴_메일을_전송한다() {
        welcomeEmailSenderAdapter.send(EMAIL, NICKNAME);

        ArgumentCaptor<EmailSendCommand> captor = ArgumentCaptor.forClass(EmailSendCommand.class);

        then(emailSender).should().send(captor.capture());

        EmailSendCommand command = captor.getValue();

        assertThat(command.from()).isEqualTo(FROM);
        assertThat(command.to()).containsExactly(EMAIL.value());
        assertThat(command.subject()).isEqualTo("[AKIVE] AKIVE에 오신 것을 환영합니다!");
        assertThat(command.templatePath()).isEqualTo("email/welcome");
        assertThat(command.variables())
                .containsEntry("nickname", NICKNAME)
                .containsEntry("supportEmail", SUPPORT_EMAIL);
    }
}
