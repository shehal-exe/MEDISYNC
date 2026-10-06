package com.medisync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisync.dto.PharmacistProfileResponse;
import com.medisync.dto.UpdatePharmacistProfileRequest;
import com.medisync.security.CustomUserDetailsService;
import com.medisync.service.PharmacistProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PharmacistProfileController.class)
@Import(com.medisync.security.SecurityConfig.class)
public class PharmacistProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PharmacistProfileService pharmacistProfileService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "pharmacist@medisync.com", roles = {"PHARMACIST"})
    void testGetMyProfile() throws Exception {
        PharmacistProfileResponse res = new PharmacistProfileResponse();
        res.setPharmacistId(1L);
        res.setFirstName("John");
        res.setLastName("Doe");
        res.setLicenseNumber("PH-12345");

        when(pharmacistProfileService.getMyProfile("pharmacist@medisync.com")).thenReturn(res);

        mockMvc.perform(get("/api/v1/pharmacist/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.licenseNumber").value("PH-12345"));
    }

    @Test
    @WithMockUser(username = "pharmacist@medisync.com", roles = {"PHARMACIST"})
    void testUpdateMyProfile() throws Exception {
        UpdatePharmacistProfileRequest req = new UpdatePharmacistProfileRequest();
        req.setFirstName("Jane");
        req.setLastName("Smith");

        PharmacistProfileResponse res = new PharmacistProfileResponse();
        res.setFirstName("Jane");

        when(pharmacistProfileService.updateMyProfile(eq("pharmacist@medisync.com"), any())).thenReturn(res);

        mockMvc.perform(put("/api/v1/pharmacist/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Jane"));
    }
}
