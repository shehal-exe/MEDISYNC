package com.medisync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisync.dto.PatientProfileResponse;
import com.medisync.dto.UpdatePatientProfileRequest;
import com.medisync.security.SecurityConfig;
import com.medisync.service.PatientProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientProfileController.class)
@Import(SecurityConfig.class)
public class PatientProfileControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private PatientProfileService patientProfileService;
    @MockBean private com.medisync.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void testUnauthenticatedProfileAccessRejected() throws Exception {
        mockMvc.perform(get("/api/v1/patients/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "pharmacist@example.com", roles = "PHARMACIST")
    void testPharmacistProfileAccessRejected() throws Exception {
        mockMvc.perform(get("/api/v1/patients/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void testPatientProfileAccessSucceeds() throws Exception {
        PatientProfileResponse response = new PatientProfileResponse();
        when(patientProfileService.getMyProfile("patient@example.com")).thenReturn(response);

        mockMvc.perform(get("/api/v1/patients/me"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void testUpdateProfileSucceeds() throws Exception {
        UpdatePatientProfileRequest request = new UpdatePatientProfileRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setContactNumber("1234567890");

        PatientProfileResponse response = new PatientProfileResponse();
        when(patientProfileService.updateMyProfile(eq("patient@example.com"), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/patients/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void testUpdateProfileValidationFails() throws Exception {
        UpdatePatientProfileRequest request = new UpdatePatientProfileRequest();
        // Missing firstName, lastName

        mockMvc.perform(put("/api/v1/patients/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
