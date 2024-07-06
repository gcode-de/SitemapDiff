package de.samuelgesang.backend.mail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class MailServiceTest {

    private JavaMailSender mailSender;
    private MailService mailService;

    @BeforeEach
    void setUp() {
        mailSender = Mockito.mock(JavaMailSender.class);
        mailService = new MailService(mailSender);
    }

    @Test
    void testSendTestEmail() {
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "Test Text";

        mailService.sendTestEmail(to, subject, text);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTo()).contains(to);
        assertThat(sentMessage.getSubject()).isEqualTo(subject);
        assertThat(sentMessage.getText()).isEqualTo(text);
    }
}
