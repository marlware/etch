package com.etch.notificationservice.web;

import com.etch.common.exception.ResourceNotFoundException;
import com.etch.dto.NotificationAuditEntryResponse;
import com.etch.dto.NotificationResponse;
import com.etch.notificationservice.domain.NotificationAuditRepository;
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
    private final NotificationAuditRepository auditRepository;

    public NotificationController(NotificationRepository notificationRepository,
                                   NotificationAuditRepository auditRepository) {
        this.notificationRepository = notificationRepository;
        this.auditRepository = auditRepository;
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<NotificationResponse>> getByOrder(@PathVariable Long orderId) {
        List<NotificationResponse> notifications = notificationRepository.findByOrderId(orderId).stream()
                .map(NotificationMapper::toResponse)
                .toList();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<NotificationAuditEntryResponse>> getHistory(@PathVariable Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("No notification found with id " + id);
        }
        List<NotificationAuditEntryResponse> history = auditRepository.findByNotificationIdOrderByTimestampAsc(id).stream()
                .map(NotificationMapper::toResponse)
                .toList();
        return ResponseEntity.ok(history);
    }
}
