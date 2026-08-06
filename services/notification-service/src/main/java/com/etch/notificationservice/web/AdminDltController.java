package com.etch.notificationservice.web;

import com.etch.dto.DltRecordResponse;
import com.etch.notificationservice.domain.NotificationDltRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dlt")
public class AdminDltController {

    private final NotificationDltRepository dltRepository;

    public AdminDltController(NotificationDltRepository dltRepository) {
        this.dltRepository = dltRepository;
    }

    @GetMapping
    public ResponseEntity<Page<DltRecordResponse>> listDeadLettered(@PageableDefault(size = 20) Pageable pageable) {
        Page<DltRecordResponse> page = dltRepository.findAllByOrderByFailedAtDesc(pageable)
                .map(NotificationMapper::toResponse);
        return ResponseEntity.ok(page);
    }
}
