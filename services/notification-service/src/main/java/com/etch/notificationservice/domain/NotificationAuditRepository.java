package com.etch.notificationservice.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationAuditRepository extends JpaRepository<NotificationAudit, Long> {

    List<NotificationAudit> findByNotificationIdOrderByTimestampAsc(Long notificationId);
}
