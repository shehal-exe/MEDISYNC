package com.medisync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisync.dto.CreateMedicationScheduleRequest;
import com.medisync.dto.MedicationScheduleResponse;
import com.medisync.dto.UpdateMedicationScheduleRequest;
import com.medisync.security.CustomUserDetailsService;
import com.medisync.service.MedicationScheduleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicationScheduleController.class)
@Import(com.medisync.security.SecurityConfig.class)
public class MedicationScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MedicationScheduleService medicationScheduleService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "patient@medisync.com", roles = {"PATIENT"})
    void testGetSchedules() throws Exception {
        MedicationScheduleResponse res = new MedicationScheduleResponse();
        res.setScheduleId(1L);
        res.setFrequency("DAILY");

        when(medicationScheduleService.getSchedules("patient@medisync.com")).thenReturn(List.of(res));

        mockMvc.perform(get("/api/v1/patient/medication-schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].scheduleId").value(1))
                .andExpect(jsonPath("$.data[0].frequency").value("DAILY"));
    }

    @Test
    @WithMockUser(username = "patient@medisync.com", roles = {"PATIENT"})
    void testCreateSchedule() throws Exception {
        CreateMedicationScheduleRequest req = new CreateMedicationScheduleRequest();
        req.setPatientMedicineId(10L);
        req.setFrequency("WEEKLY");
        req.setTimeOfDay(LocalTime.of(8, 0));
        req.setStartDate(LocalDate.now());

        MedicationScheduleResponse res = new MedicationScheduleResponse();
        res.setScheduleId(1L);
        res.setFrequency("WEEKLY");

        when(medicationScheduleService.createSchedule(eq("patient@medisync.com"), any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/patient/medication-schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scheduleId").value(1));
    }

    @Test
    @WithMockUser(username = "patient@medisync.com", roles = {"PATIENT"})
    void testCreateSchedule_InvalidInput() throws Exception {
        CreateMedicationScheduleRequest req = new CreateMedicationScheduleRequest();
        req.setPatientMedicineId(10L);
        // Missing frequency, timeOfDay, startDate

        mockMvc.perform(post("/api/v1/patient/medication-schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "patient@medisync.com", roles = {"PATIENT"})
    void testGetScheduleById_NotFound() throws Exception {
        when(medicationScheduleService.getScheduleById("patient@medisync.com", 999L))
                .thenThrow(new IllegalArgumentException("Not found"));

        mockMvc.perform(get("/api/v1/patient/medication-schedules/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
