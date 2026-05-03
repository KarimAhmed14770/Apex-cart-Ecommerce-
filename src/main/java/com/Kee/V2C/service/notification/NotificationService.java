package com.Kee.V2C.service.notification;

import com.Kee.V2C.events.OrderPlacedEvent;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationService {
    SseEmitter subscribe();
    String createTicket();
    Long validateAndRemoveTicket(String ticket);
    void handleOrders(OrderPlacedEvent orderPlacedEvent);
}
