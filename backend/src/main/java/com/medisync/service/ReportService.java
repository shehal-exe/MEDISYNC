package com.medisync.service;

import com.medisync.dao.PharmacistProfileDao;
import com.medisync.dao.ReportDao;
import com.medisync.dao.UserDao;
import com.medisync.dto.DashboardReportResponse;
import com.medisync.dto.PharmacistProfileResponse;
import com.medisync.model.User;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final ReportDao reportDao;
    private final PharmacistProfileDao pharmacistProfileDao;
    private final UserDao userDao;

    public ReportService(ReportDao reportDao, PharmacistProfileDao pharmacistProfileDao, UserDao userDao) {
        this.reportDao = reportDao;
        this.pharmacistProfileDao = pharmacistProfileDao;
        this.userDao = userDao;
    }

    public DashboardReportResponse getDashboardReport(String email) {
        Long pharmacistId = getPharmacistId(email);
        DashboardReportResponse report = new DashboardReportResponse();
        report.setTotalSalesCount(reportDao.getTotalSalesCount(pharmacistId));
        report.setTotalRevenue(reportDao.getTotalRevenue(pharmacistId));
        report.setLowStockMedicines(reportDao.getLowStockMedicines(pharmacistId, 10)); // Threshold of 10
        return report;
    }

    private Long getPharmacistId(String email) {
        User user = userDao.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("User not found");
        PharmacistProfileResponse profile = pharmacistProfileDao.getProfileByUserId(user.getUserId());
        if (profile == null) throw new IllegalArgumentException("Pharmacist profile not found");
        return profile.getPharmacistId();
    }
}
