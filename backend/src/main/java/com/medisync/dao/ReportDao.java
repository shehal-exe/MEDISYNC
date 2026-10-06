package com.medisync.dao;

import com.medisync.dto.MedicineResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class ReportDao {

    private final JdbcTemplate jdbcTemplate;

    public ReportDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long getTotalSalesCount(Long pharmacistId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Sale WHERE pharmacist_id = ?",
                Long.class,
                pharmacistId
        );
        return count != null ? count : 0L;
    }

    public BigDecimal getTotalRevenue(Long pharmacistId) {
        BigDecimal revenue = jdbcTemplate.queryForObject(
                "SELECT SUM(total_amount) FROM Sale WHERE pharmacist_id = ?",
                BigDecimal.class,
                pharmacistId
        );
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public List<MedicineResponse> getLowStockMedicines(Long pharmacistId, int threshold) {
        RowMapper<MedicineResponse> rowMapper = (rs, rowNum) -> {
            MedicineResponse m = new MedicineResponse();
            m.setMedicineId(rs.getLong("medicine_id"));
            m.setName(rs.getString("name"));
            m.setDescription(rs.getString("description"));
            m.setManufacturer(rs.getString("manufacturer"));
            m.setPrice(rs.getBigDecimal("price") != null ? rs.getBigDecimal("price") : BigDecimal.ZERO);
            m.setStockQuantity(rs.getInt("stock_quantity"));
            m.setRequiresPrescription(false);
            return m;
        };
        String sql = "SELECT m.medicine_id, m.name, m.description, m.manufacturer, " +
                     "COALESCE(SUM(ib.quantity_in_stock), 0) AS stock_quantity, " +
                     "MAX(ib.unit_price) AS price " +
                     "FROM Medicine m " +
                     "LEFT JOIN InventoryBatch ib ON m.medicine_id = ib.medicine_id " +
                     "WHERE m.pharmacist_id = ? " +
                     "GROUP BY m.medicine_id, m.name, m.description, m.manufacturer " +
                     "HAVING stock_quantity <= ? " +
                     "ORDER BY stock_quantity ASC";
        return jdbcTemplate.query(sql, rowMapper, pharmacistId, threshold);
    }
}
