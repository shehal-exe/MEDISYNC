package com.medisync.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardReportResponse {
    private Long totalSalesCount;
    private BigDecimal totalRevenue;
    private List<MedicineResponse> lowStockMedicines;

    public Long getTotalSalesCount() { return totalSalesCount; }
    public void setTotalSalesCount(Long totalSalesCount) { this.totalSalesCount = totalSalesCount; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public List<MedicineResponse> getLowStockMedicines() { return lowStockMedicines; }
    public void setLowStockMedicines(List<MedicineResponse> lowStockMedicines) { this.lowStockMedicines = lowStockMedicines; }
}
