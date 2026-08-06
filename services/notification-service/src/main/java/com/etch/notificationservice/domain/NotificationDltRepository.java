package com.etch.notificationservice.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationDltRepository extends JpaRepository<NotificationDlt, Long> {

    Page<NotificationDlt> findAllByOrderByFailedAtDesc(Pageable pageable);
}
