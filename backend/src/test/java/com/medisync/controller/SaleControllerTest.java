package com.medisync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisync.dto.CreateSaleRequest;
import com.medisync.dto.SaleItemRequest;
import com.medisync.dto.SaleResponse;
import com.medisync.security.CustomUserDetailsService;
import com.medisync.service.SaleService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SaleController.class)
@Import(com.medisync.security.SecurityConfig.class)
public class SaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SaleService saleService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "pharmacist@medisync.com", roles = {"PHARMACIST"})
    void testProcessSale() throws Exception {
        CreateSaleRequest req = new CreateSaleRequest();
        SaleItemRequest itemReq = new SaleItemRequest();
        itemReq.setMedicineId(1L);
        itemReq.setQuantity(2);
        req.setItems(List.of(itemReq));

        SaleResponse res = new SaleResponse();
        res.setSaleId(1L);
        res.setTotalAmount(new BigDecimal("10.00"));
        res.setPaymentStatus("COMPLETED");

        when(saleService.createSale(eq("pharmacist@medisync.com"), any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/pharmacist/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalAmount").value(10.00));
    }

    @Test
    @WithMockUser(username = "pharmacist@medisync.com", roles = {"PHARMACIST"})
    void testGetAllSales() throws Exception {
        SaleResponse res = new SaleResponse();
        res.setSaleId(1L);
        res.setTotalAmount(new BigDecimal("10.00"));

        when(saleService.getAllSales()).thenReturn(List.of(res));

        mockMvc.perform(get("/api/v1/pharmacist/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].saleId").value(1));
    }
}
