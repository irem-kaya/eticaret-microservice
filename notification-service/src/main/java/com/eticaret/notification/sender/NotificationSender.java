package com.eticaret.notification.sender;

public interface NotificationSender {
    void send(String to, String subject, String body);
    String getChannel();
}