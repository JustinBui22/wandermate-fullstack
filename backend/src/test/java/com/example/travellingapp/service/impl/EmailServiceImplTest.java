package com.example.travellingapp.service.impl;

import com.example.travellingapp.entity.ConfigurationEntity;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.ConfigurationRepository;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.time.Instant;
import java.util.Optional;
import java.util.Properties;

import static com.example.travellingapp.enums.CommonEnum.EMAIL;
import static com.example.travellingapp.enums.CommonEnum.EMAIL_ACCESS_TOKEN_CONFIG;
import static com.example.travellingapp.enums.ErrorCodeEnum.EMAIL_SENT_FAIL;
import static com.example.travellingapp.enums.ErrorCodeEnum.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSenderImpl mailSender;

    @Mock
    private ConfigurationRepository configurationRepository;

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender, configurationRepository);
    }

    @Test
    void sendEmail_shouldSendHtmlEmailSuccessfully() throws Exception {
        MimeMessage mimeMessage = createMimeMessage();

        mockEmailAccessToken("latest-access-token");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail(
                "sender@example.com",
                "receiver@example.com",
                "Your OTP code",
                "<p>Your OTP is <strong>123456</strong></p>"
        );

        verify(mailSender).setPassword("latest-access-token");
        verify(mailSender).send(mimeMessage);

        assertThat(mimeMessage.getSubject()).isEqualTo("Your OTP code");
        assertThat(mimeMessage.getFrom()[0].toString()).isEqualTo("sender@example.com");
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo("receiver@example.com");

        String content = extractMessageContent(mimeMessage.getContent());
        assertThat(content).contains("Your OTP is");
        assertThat(content).contains("123456");
    }

    @Test
    void sendEmail_shouldSetLatestAccessTokenBeforeSendingEmail() {
        MimeMessage mimeMessage = createMimeMessage();

        mockEmailAccessToken("latest-access-token");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail(
                "sender@example.com",
                "receiver@example.com",
                "Subject",
                "<p>Content</p>"
        );

        InOrder inOrder = inOrder(mailSender);

        inOrder.verify(mailSender).createMimeMessage();
        inOrder.verify(mailSender).setPassword("latest-access-token");
        inOrder.verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_shouldReadFreshAccessTokenForEachEmailSend() {
        MimeMessage firstMessage = createMimeMessage();
        MimeMessage secondMessage = createMimeMessage();

        ConfigurationEntity firstToken = config(EMAIL_ACCESS_TOKEN_CONFIG.name(), "access-token-1");
        ConfigurationEntity secondToken = config(EMAIL_ACCESS_TOKEN_CONFIG.name(), "access-token-2");

        when(configurationRepository.findByConfigCode(EMAIL_ACCESS_TOKEN_CONFIG.name()))
                .thenReturn(Optional.of(firstToken), Optional.of(secondToken));

        when(mailSender.createMimeMessage())
                .thenReturn(firstMessage, secondMessage);

        emailService.sendEmail(
                "sender@example.com",
                "first@example.com",
                "First email",
                "<p>First</p>"
        );

        emailService.sendEmail(
                "sender@example.com",
                "second@example.com",
                "Second email",
                "<p>Second</p>"
        );

        verify(mailSender).setPassword("access-token-1");
        verify(mailSender).setPassword("access-token-2");
        verify(mailSender).send(firstMessage);
        verify(mailSender).send(secondMessage);
    }

    @Test
    void sendEmail_shouldThrowInternalServerError_whenAccessTokenConfigMissing() {
        MimeMessage mimeMessage = createMimeMessage();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(configurationRepository.findByConfigCode(EMAIL_ACCESS_TOKEN_CONFIG.name()))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> emailService.sendEmail(
                        "sender@example.com",
                        "receiver@example.com",
                        "Subject",
                        "<p>Content</p>"
                )
        );

        assertThat(exception.getErrorCodeEnum()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(exception.getFlow()).isEqualTo(EMAIL.name());

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_shouldThrowEmailSentFail_whenMimeMessageSetupThrowsMessagingException() {
        MimeMessage throwingMessage = new ThrowingRecipientMimeMessage();

        mockEmailAccessToken("latest-access-token");
        when(mailSender.createMimeMessage()).thenReturn(throwingMessage);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> emailService.sendEmail(
                        "sender@example.com",
                        "receiver@example.com",
                        "Subject",
                        "<p>Content</p>"
                )
        );

        assertThat(exception.getErrorCodeEnum()).isEqualTo(EMAIL_SENT_FAIL);
        assertThat(exception.getFlow()).isEqualTo(EMAIL.name());

        verify(mailSender).setPassword("latest-access-token");
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_shouldThrowInternalServerError_whenCreateMimeMessageFails() {
        when(mailSender.createMimeMessage())
                .thenThrow(new RuntimeException("Cannot create message"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> emailService.sendEmail(
                        "sender@example.com",
                        "receiver@example.com",
                        "Subject",
                        "<p>Content</p>"
                )
        );

        assertThat(exception.getErrorCodeEnum()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(exception.getFlow()).isEqualTo(EMAIL.name());

        verify(configurationRepository, never()).findByConfigCode(anyString());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_shouldThrowInternalServerError_whenMailSenderAuthenticationFails() {
        MimeMessage mimeMessage = createMimeMessage();

        mockEmailAccessToken("latest-access-token");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        doThrow(new MailAuthenticationException("Bad credentials"))
                .when(mailSender)
                .send(mimeMessage);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> emailService.sendEmail(
                        "sender@example.com",
                        "receiver@example.com",
                        "Subject",
                        "<p>Content</p>"
                )
        );

        assertThat(exception.getErrorCodeEnum()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(exception.getFlow()).isEqualTo(EMAIL.name());

        verify(mailSender).setPassword("latest-access-token");
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_shouldThrowInternalServerError_whenMailSenderSendFailsGenerally() {
        MimeMessage mimeMessage = createMimeMessage();

        mockEmailAccessToken("latest-access-token");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        doThrow(new RuntimeException("SMTP server unavailable"))
                .when(mailSender)
                .send(mimeMessage);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> emailService.sendEmail(
                        "sender@example.com",
                        "receiver@example.com",
                        "Subject",
                        "<p>Content</p>"
                )
        );

        assertThat(exception.getErrorCodeEnum()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(exception.getFlow()).isEqualTo(EMAIL.name());
    }

    @Test
    void sendEmail_shouldThrowInternalServerError_whenSetPasswordFails() {
        MimeMessage mimeMessage = createMimeMessage();

        mockEmailAccessToken("latest-access-token");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        doThrow(new RuntimeException("Cannot update mail password"))
                .when(mailSender)
                .setPassword("latest-access-token");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> emailService.sendEmail(
                        "sender@example.com",
                        "receiver@example.com",
                        "Subject",
                        "<p>Content</p>"
                )
        );

        assertThat(exception.getErrorCodeEnum()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(exception.getFlow()).isEqualTo(EMAIL.name());

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    private MimeMessage createMimeMessage() {
        return new MimeMessage(Session.getDefaultInstance(new Properties()));
    }

    private void mockEmailAccessToken(String token) {
        when(configurationRepository.findByConfigCode(EMAIL_ACCESS_TOKEN_CONFIG.name()))
                .thenReturn(Optional.of(config(EMAIL_ACCESS_TOKEN_CONFIG.name(), token)));
    }

    private ConfigurationEntity config(String configCode, String configValue) {
        ConfigurationEntity entity = new ConfigurationEntity();
        entity.setConfigCode(configCode);
        entity.setConfigValue(configValue);
        entity.setCreatedDate(Instant.now());
        return entity;
    }

    private String extractMessageContent(Object content) throws Exception {
        if (content instanceof String text) {
            return text;
        }

        if (content instanceof Multipart multipart) {
            StringBuilder result = new StringBuilder();

            for (int i = 0; i < multipart.getCount(); i++) {
                Object bodyPartContent = multipart.getBodyPart(i).getContent();
                result.append(extractMessageContent(bodyPartContent));
            }

            return result.toString();
        }

        return "";
    }

    private static class ThrowingRecipientMimeMessage extends MimeMessage {
        private ThrowingRecipientMimeMessage() {
            super(Session.getDefaultInstance(new Properties()));
        }

        @Override
        public void setRecipients(Message.RecipientType type, Address[] addresses)
                throws MessagingException {
            throw new MessagingException("Failed to set recipient");
        }
    }
}