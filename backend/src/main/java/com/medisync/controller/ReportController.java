package com.medisync.controller;

import com.medisync.dto.ApiResponse;
import com.medisync.dto.DashboardReportResponse;
import com.medisync.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pharmacist/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardReportResponse>> getDashboard() {
        DashboardReportResponse report = reportService.getDashboardReport();
        return ResponseEntity.ok(new ApiResponse<>(true, "Dashboard report retrieved successfully", report));
    }
}
