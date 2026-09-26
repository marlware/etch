package com.etch.notificationservice.web;

import com.etch.events.NotificationChannel;
import com.etch.notificationservice.config.GlobalExceptionHandler;
import com.etch.notificationservice.domain.NotificationDlt;
import com.etch.notificationservice.domain.NotificationDltRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminDltController.class)
@Import(GlobalExceptionHandler.class)
class AdminDltControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationDltRepository dltRepository;

    @Test
    void listsDeadLetteredNotifications() throws Exception {
        NotificationDlt record = new NotificationDlt(
                3L, 10L, NotificationChannel.SMS, "{\"orderId\":10}", "Carrier rejected the message", 3);
        when(dltRepository.findAllByOrderByFailedAtDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(record)));

        mockMvc.perform(get("/admin/dlt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].orderId").value(10))
                .andExpect(jsonPath("$.content[0].channel").value("SMS"))
                .andExpect(jsonPath("$.content[0].failureReason").value("Carrier rejected the message"))
                .andExpect(jsonPath("$.content[0].retryCount").value(3));
    }
}
