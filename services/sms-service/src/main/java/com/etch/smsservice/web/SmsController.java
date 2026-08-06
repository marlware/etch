package com.etch.smsservice.web;

import com.etch.dto.SendMessageRequest;
import com.etch.dto.SendMessageResponse;
import com.etch.smsservice.service.SmsSendService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsController {

    private final SmsSendService smsSendService;

    public SmsController(SmsSendService smsSendService) {
        this.smsSendService = smsSendService;
    }

    @PostMapping("/send")
    public ResponseEntity<SendMessageResponse> send(@Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(smsSendService.send(request));
    }
}
