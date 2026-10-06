package com.medisync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisync.dto.CreatePatientMedicineRequest;
import com.medisync.dto.PatientMedicineResponse;
import com.medisync.dto.UpdatePatientMedicineRequest;
import com.medisync.security.SecurityConfig;
import com.medisync.service.PatientMedicineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientMedicineController.class)
@Import(SecurityConfig.class)
public class PatientMedicineControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private PatientMedicineService patientMedicineService;
    @MockBean private com.medisync.security.CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "pharmacist@example.com", roles = "PHARMACIST")
    void testPharmacistAccessRejected() throws Exception {
        mockMvc.perform(get("/api/v1/patient/medicines")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void testGetMyMedicines() throws Exception {
        when(patientMedicineService.getMyMedicines("patient@example.com")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/patient/medicines"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void testGetMyMedicineByIdSuccess() throws Exception {
        when(patientMedicineService.getMyMedicineById("patient@example.com", 1L)).thenReturn(new PatientMedicineResponse());

        mockMvc.perform(get("/api/v1/patient/medicines/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void testGetMyMedicineByIdForbidden() throws Exception {
        when(patientMedicineService.getMyMedicineById("patient@example.com", 2L))
                .thenThrow(new SecurityException("Access denied"));

        mockMvc.perform(get("/api/v1/patient/medicines/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void testAddMedicineSuccess() throws Exception {
        CreatePatientMedicineRequest request = new CreatePatientMedicineRequest();
        request.setMedicineId(10L);
        request.setDosage("1 pill");

        when(patientMedicineService.addMedicine(eq("patient@example.com"), any())).thenReturn(new PatientMedicineResponse());

        mockMvc.perform(post("/api/v1/patient/medicines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void testAddMedicineInvalidId() throws Exception {
        CreatePatientMedicineRequest request = new CreatePatientMedicineRequest();
        request.setMedicineId(999L);
        request.setDosage("1 pill");

        when(patientMedicineService.addMedicine(eq("patient@example.com"), any()))
                .thenThrow(new IllegalArgumentException("Invalid medicine ID"));

        mockMvc.perform(post("/api/v1/patient/medicines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void testUpdateMedicineSuccess() throws Exception {
        UpdatePatientMedicineRequest request = new UpdatePatientMedicineRequest();
        request.setDosage("2 pills");
        
        when(patientMedicineService.updateMedicine(eq("patient@example.com"), eq(1L), any())).thenReturn(new PatientMedicineResponse());

        mockMvc.perform(put("/api/v1/patient/medicines/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void testDeleteMedicineSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/patient/medicines/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void testDeleteMedicineForbidden() throws Exception {
        doThrow(new SecurityException("Access denied")).when(patientMedicineService).deleteMedicine("patient@example.com", 2L);

        mockMvc.perform(delete("/api/v1/patient/medicines/2"))
                .andExpect(status().isForbidden());
    }
}
