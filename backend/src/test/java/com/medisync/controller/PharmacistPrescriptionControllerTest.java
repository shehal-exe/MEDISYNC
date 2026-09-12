package com.medisync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisync.dto.PrescriptionResponse;
import com.medisync.dto.UpdatePrescriptionStatusRequest;
import com.medisync.security.CustomUserDetailsService;
import com.medisync.service.PharmacistPrescriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PharmacistPrescriptionController.class)
@Import(com.medisync.security.SecurityConfig.class)
public class PharmacistPrescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PharmacistPrescriptionService pharmacistPrescriptionService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "pharmacist@medisync.com", roles = {"PHARMACIST"})
    void testGetAllPrescriptions() throws Exception {
        PrescriptionResponse res = new PrescriptionResponse();
        res.setPrescriptionId(1L);
        res.setStatus("PENDING");

        when(pharmacistPrescriptionService.getAllPrescriptions(null)).thenReturn(List.of(res));

        mockMvc.perform(get("/api/v1/pharmacist/prescriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "pharmacist@medisync.com", roles = {"PHARMACIST"})
    void testUpdateStatus() throws Exception {
        UpdatePrescriptionStatusRequest req = new UpdatePrescriptionStatusRequest();
        req.setStatus("VERIFIED");
        req.setNotes("Looks good.");

        PrescriptionResponse res = new PrescriptionResponse();
        res.setPrescriptionId(1L);
        res.setStatus("VERIFIED");

        when(pharmacistPrescriptionService.updateStatus(eq("pharmacist@medisync.com"), eq(1L), any())).thenReturn(res);

        mockMvc.perform(put("/api/v1/pharmacist/prescriptions/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("VERIFIED"));
    }
}
