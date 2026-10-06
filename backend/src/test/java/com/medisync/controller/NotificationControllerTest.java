package com.medisync.controller;

import com.medisync.dto.NotificationResponse;
import com.medisync.security.CustomUserDetailsService;
import com.medisync.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import(com.medisync.security.SecurityConfig.class)
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "user@medisync.com", roles = {"PATIENT"})
    void testGetMyNotifications() throws Exception {
        NotificationResponse res = new NotificationResponse();
        res.setNotificationId(1L);
        res.setMessage("Test notification");
        res.setIsRead(false);

        when(notificationService.getMyNotifications("user@medisync.com")).thenReturn(List.of(res));

        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].message").value("Test notification"));
    }

    @Test
    @WithMockUser(username = "user@medisync.com", roles = {"PATIENT"})
    void testMarkAsRead() throws Exception {
        mockMvc.perform(put("/api/v1/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
