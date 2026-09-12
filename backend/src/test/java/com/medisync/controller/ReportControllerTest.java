package com.medisync.controller;

import com.medisync.dto.DashboardReportResponse;
import com.medisync.security.CustomUserDetailsService;
import com.medisync.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@Import(com.medisync.security.SecurityConfig.class)
public class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "pharmacist@medisync.com", roles = {"PHARMACIST"})
    void testGetDashboard() throws Exception {
        DashboardReportResponse res = new DashboardReportResponse();
        res.setTotalSalesCount(100L);
        res.setTotalRevenue(new BigDecimal("1500.50"));
        res.setLowStockMedicines(new ArrayList<>());

        when(reportService.getDashboardReport()).thenReturn(res);

        mockMvc.perform(get("/api/v1/pharmacist/reports/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalSalesCount").value(100))
                .andExpect(jsonPath("$.data.totalRevenue").value(1500.50));
    }
}
