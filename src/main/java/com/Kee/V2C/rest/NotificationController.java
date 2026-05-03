package com.Kee.V2C.rest;


import com.Kee.V2C.service.notification.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService){
        this.notificationService=notificationService;
    }

    @PostMapping("/ticket")
    public ResponseEntity<String> getValidationTicket(){
        return ResponseEntity.ok(notificationService.createTicket());
    }


    @GetMapping("/stream")
    public SseEmitter openSseConnection(){
        return notificationService.subscribe();
    }
}
