package com.medisync.service;

import com.medisync.dao.ReportDao;
import com.medisync.dto.DashboardReportResponse;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final ReportDao reportDao;

    public ReportService(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    public DashboardReportResponse getDashboardReport() {
        DashboardReportResponse report = new DashboardReportResponse();
        report.setTotalSalesCount(reportDao.getTotalSalesCount());
        report.setTotalRevenue(reportDao.getTotalRevenue());
        report.setLowStockMedicines(reportDao.getLowStockMedicines(10)); // Threshold of 10
        return report;
    }
}
