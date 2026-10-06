package com.medisync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisync.dto.AdherenceResponse;
import com.medisync.dto.MedicationLogResponse;
import com.medisync.dto.ReminderResponse;
import com.medisync.security.CustomUserDetailsService;
import com.medisync.service.MedicationReminderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicationReminderController.class)
@Import(com.medisync.security.SecurityConfig.class)
public class MedicationReminderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicationReminderService medicationReminderService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "patient@medisync.com", roles = {"PATIENT"})
    void testGetReminders() throws Exception {
        ReminderResponse res = new ReminderResponse();
        res.setScheduleId(1L);
        res.setMedicineName("Amoxicillin");

        when(medicationReminderService.getUpcomingReminders("patient@medisync.com")).thenReturn(List.of(res));

        mockMvc.perform(get("/api/v1/patient/reminders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].scheduleId").value(1))
                .andExpect(jsonPath("$.data[0].medicineName").value("Amoxicillin"));
    }

    @Test
    @WithMockUser(username = "patient@medisync.com", roles = {"PATIENT"})
    void testMarkTaken() throws Exception {
        mockMvc.perform(post("/api/v1/patient/reminders/1/taken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "patient@medisync.com", roles = {"PATIENT"})
    void testGetHistory() throws Exception {
        MedicationLogResponse res = new MedicationLogResponse();
        res.setLogId(1L);
        res.setStatus("TAKEN");

        when(medicationReminderService.getHistory("patient@medisync.com")).thenReturn(List.of(res));

        mockMvc.perform(get("/api/v1/patient/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("TAKEN"));
    }

    @Test
    @WithMockUser(username = "patient@medisync.com", roles = {"PATIENT"})
    void testGetAdherence() throws Exception {
        AdherenceResponse res = new AdherenceResponse();
        res.setTotalTaken(10);
        res.setTotalSkipped(2);
        res.setAdherencePercentage(83.33);

        when(medicationReminderService.getAdherence("patient@medisync.com")).thenReturn(res);

        mockMvc.perform(get("/api/v1/patient/adherence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.adherencePercentage").value(83.33));
    }
}
