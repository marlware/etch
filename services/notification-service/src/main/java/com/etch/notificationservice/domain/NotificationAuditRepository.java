package com.etch.notificationservice.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationAuditRepository extends JpaRepository<NotificationAudit, Long> {
}
