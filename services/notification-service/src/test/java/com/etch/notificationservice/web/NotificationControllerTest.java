package com.etch.notificationservice.web;

import com.etch.events.NotificationChannel;
import com.etch.notificationservice.config.GlobalExceptionHandler;
import com.etch.notificationservice.domain.Notification;
import com.etch.notificationservice.domain.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@Import(GlobalExceptionHandler.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationRepository notificationRepository;

    @Test
    void listsNotificationsForAnOrder() throws Exception {
        Notification notification = new Notification(10L, NotificationChannel.EMAIL, "buyer@example.com", "corr-1");
        when(notificationRepository.findByOrderId(10L)).thenReturn(List.of(notification));

        mockMvc.perform(get("/notifications/order/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value(10))
                .andExpect(jsonPath("$[0].channel").value("EMAIL"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void returnsAnEmptyListWhenAnOrderHasNoNotifications() throws Exception {
        when(notificationRepository.findByOrderId(99L)).thenReturn(List.of());

        mockMvc.perform(get("/notifications/order/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
