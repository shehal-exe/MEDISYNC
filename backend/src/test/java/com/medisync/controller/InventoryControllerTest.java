package com.medisync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisync.dto.MedicineRequest;
import com.medisync.dto.MedicineResponse;
import com.medisync.security.CustomUserDetailsService;
import com.medisync.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
@Import(com.medisync.security.SecurityConfig.class)
public class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "pharmacist@medisync.com", roles = {"PHARMACIST"})
    void testGetAll() throws Exception {
        MedicineResponse res = new MedicineResponse();
        res.setMedicineId(1L);
        res.setName("Aspirin");
        res.setPrice(new BigDecimal("5.99"));

        when(inventoryService.getAllMedicines()).thenReturn(List.of(res));

        mockMvc.perform(get("/api/v1/pharmacist/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Aspirin"));
    }

    @Test
    @WithMockUser(username = "pharmacist@medisync.com", roles = {"PHARMACIST"})
    void testAddMedicine() throws Exception {
        MedicineRequest req = new MedicineRequest();
        req.setName("Ibuprofen");
        req.setPrice(new BigDecimal("8.50"));
        req.setStockQuantity(100);

        MedicineResponse res = new MedicineResponse();
        res.setMedicineId(2L);
        res.setName("Ibuprofen");

        when(inventoryService.addMedicine(any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/pharmacist/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Ibuprofen"));
    }

    @Test
    @WithMockUser(username = "pharmacist@medisync.com", roles = {"PHARMACIST"})
    void testDeleteMedicine() throws Exception {
        doNothing().when(inventoryService).deleteMedicine(1L);

        mockMvc.perform(delete("/api/v1/pharmacist/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
