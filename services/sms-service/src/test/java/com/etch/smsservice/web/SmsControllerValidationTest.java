package com.etch.smsservice.web;

import com.etch.dto.SendMessageResponse;
import com.etch.smsservice.config.GlobalExceptionHandler;
import com.etch.smsservice.service.SmsSendService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SmsController.class)
@Import(GlobalExceptionHandler.class)
class SmsControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SmsSendService smsSendService;

    @Test
    void rejectsRequestMissingRecipient() throws Exception {
        mockMvc.perform(post("/sms/send")
                        .contentType("application/json")
                        .content("{\"body\":\"hello\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsRequestMissingBody() throws Exception {
        mockMvc.perform(post("/sms/send")
                        .contentType("application/json")
                        .content("{\"recipient\":\"+15551234567\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void returnsTheProviderResultForAValidRequest() throws Exception {
        when(smsSendService.send(any())).thenReturn(SendMessageResponse.success("sms-1"));

        mockMvc.perform(post("/sms/send")
                        .contentType("application/json")
                        .content("{\"recipient\":\"+15551234567\",\"body\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.providerMessageId").value("sms-1"));
    }
}
