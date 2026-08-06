package com.etch.notificationservice.web;

import com.etch.dto.NotificationResponse;
import com.etch.notificationservice.domain.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<NotificationResponse>> getByOrder(@PathVariable Long orderId) {
        List<NotificationResponse> notifications = notificationRepository.findByOrderId(orderId).stream()
                .map(NotificationMapper::toResponse)
                .toList();
        return ResponseEntity.ok(notifications);
    }
}
