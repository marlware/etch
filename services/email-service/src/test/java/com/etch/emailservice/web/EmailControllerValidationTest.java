package com.etch.emailservice.web;

import com.etch.emailservice.config.GlobalExceptionHandler;
import com.etch.emailservice.service.EmailSendService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EmailController.class)
@org.springframework.context.annotation.Import(GlobalExceptionHandler.class)
class EmailControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailSendService emailSendService;

    @Test
    void rejectsRequestMissingRecipient() throws Exception {
        mockMvc.perform(post("/email/send")
                        .contentType("application/json")
                        .content("{\"body\":\"hello\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsRequestMissingBody() throws Exception {
        mockMvc.perform(post("/email/send")
                        .contentType("application/json")
                        .content("{\"recipient\":\"buyer@example.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
