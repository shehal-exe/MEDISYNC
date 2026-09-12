package com.medisync.controller;

import com.medisync.dto.PrescriptionResponse;
import com.medisync.security.CustomUserDetailsService;
import com.medisync.service.PatientPrescriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientPrescriptionController.class)
@Import(com.medisync.security.SecurityConfig.class)
public class PatientPrescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientPrescriptionService patientPrescriptionService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "patient@medisync.com", roles = {"PATIENT"})
    void testUploadPrescription() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
        
        PrescriptionResponse res = new PrescriptionResponse();
        res.setPrescriptionId(1L);
        res.setStatus("PENDING");

        when(patientPrescriptionService.uploadPrescription(eq("patient@medisync.com"), any(), any())).thenReturn(res);

        mockMvc.perform(multipart("/api/v1/patient/prescriptions/upload")
                .file(file)
                .param("notes", "Please verify quickly"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.prescriptionId").value(1));
    }

    @Test
    @WithMockUser(username = "patient@medisync.com", roles = {"PATIENT"})
    void testGetMyPrescriptions() throws Exception {
        PrescriptionResponse res = new PrescriptionResponse();
        res.setPrescriptionId(1L);
        res.setStatus("VERIFIED");

        when(patientPrescriptionService.getMyPrescriptions("patient@medisync.com")).thenReturn(List.of(res));

        mockMvc.perform(get("/api/v1/patient/prescriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("VERIFIED"));
    }
}
