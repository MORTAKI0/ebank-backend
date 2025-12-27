package com.ebank.backend.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final Environment environment;

    public MailService(ObjectProvider<JavaMailSender> mailSenderProvider, Environment environment) {
        this.mailSenderProvider = mailSenderProvider;
        this.environment = environment;
    }

    public void sendNewCustomerCredentials(String toEmail, String username, String rawPassword) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        // If SMTP not configured OR sender bean not created => fallback
        if (mailSender == null || !isMailConfigured()) {
            logFallback(toEmail, username, rawPassword);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Vos identifiants eBank");
        message.setText("Bonjour,\n\nVotre compte a ete cree.\n"
                + "Login: " + username + "\n"
                + "Mot de passe: " + rawPassword + "\n\n"
                + "Merci.");

        try {
            mailSender.send(message);
        } catch (Exception ex) {
            logger.warn("Email send failed, fallback to console log: {}", ex.getMessage());
            logFallback(toEmail, username, rawPassword);
        }
    }

    private boolean isMailConfigured() {
        String host = environment.getProperty("spring.mail.host");
        return host != null && !host.isBlank();
    }

    private void logFallback(String toEmail, String username, String rawPassword) {
        logger.info("Mail fallback - to: {}, login: {}, password: {}", toEmail, username, rawPassword);
    }
}
