package com.bookhub.identity.infrastructure.mail;

import com.bookhub.identity.config.PasswordResetProperties;
import com.bookhub.identity.domain.auth.MailSenderPort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class JavaMailSenderAdapter implements MailSenderPort {

    private static final String RESET_SUBJECT = "BookHub password reset";

    private final JavaMailSender javaMailSender;
    private final PasswordResetProperties passwordResetProperties;

    public JavaMailSenderAdapter(
            final JavaMailSender javaMailSender,
            final PasswordResetProperties passwordResetProperties) {
        this.javaMailSender = javaMailSender;
        this.passwordResetProperties = passwordResetProperties;
    }

    @Override
    public void sendPasswordResetEmail(final String to, final String token) {
        final SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(passwordResetProperties.fromAddress());
        message.setTo(to);
        message.setSubject(RESET_SUBJECT);
        message.setText(buildBody(token));
        javaMailSender.send(message);
    }

    private String buildBody(final String token) {
        return "You requested a password reset for your BookHub account.\n\n"
                + "Use this link to set a new password:\n"
                + passwordResetProperties.resetUrlBase()
                + token
                + "\n\nIf you did not request this, you can safely ignore this email.";
    }
}
