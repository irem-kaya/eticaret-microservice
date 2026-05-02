package com.eticaret.notification.factory;

import com.eticaret.notification.sender.NotificationSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class NotificationFactory {

    private final Map<String, NotificationSender> senders;

    @Value("${app.notification.default-channel:email}")
    private String defaultChannel;

    public NotificationFactory(Map<String, NotificationSender> senders) {
        this.senders = senders;
    }

    public NotificationSender getSender() {
        return getSender(defaultChannel);
    }

    public NotificationSender getSender(String channel) {
        NotificationSender sender = senders.get(channel);
        if (sender == null) {
            throw new IllegalArgumentException("Bilinmeyen kanal: " + channel);
        }
        return sender;
    }
}