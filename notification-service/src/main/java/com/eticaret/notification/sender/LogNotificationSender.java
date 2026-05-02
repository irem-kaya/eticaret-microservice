package com.eticaret.notification.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component("log")
@ConditionalOnProperty(name = "app.notification.mock", havingValue = "true")
public class LogNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(LogNotificationSender.class);

    @Override
    public void send(String to, String subject, String body) {
        log.info("📧 [MOCK] To: {} | Subject: {} | Body: {}", to, subject, body);
    }

    @Override
    public String getChannel() {
        return "log";
    }
}