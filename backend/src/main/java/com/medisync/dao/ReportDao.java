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

    public Long getTotalSalesCount() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Sale", Long.class);
        return count != null ? count : 0L;
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = jdbcTemplate.queryForObject("SELECT SUM(total_amount) FROM Sale", BigDecimal.class);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public List<MedicineResponse> getLowStockMedicines(int threshold) {
        RowMapper<MedicineResponse> rowMapper = (rs, rowNum) -> {
            MedicineResponse m = new MedicineResponse();
            m.setMedicineId(rs.getLong("medicine_id"));
            m.setName(rs.getString("name"));
            m.setDescription(rs.getString("description"));
            m.setManufacturer(rs.getString("manufacturer"));
            m.setPrice(rs.getBigDecimal("price"));
            m.setStockQuantity(rs.getInt("stock_quantity"));
            m.setRequiresPrescription(rs.getBoolean("requires_prescription"));
            return m;
        };
        return jdbcTemplate.query("SELECT * FROM Medicine WHERE stock_quantity <= ? ORDER BY stock_quantity ASC", rowMapper, threshold);
    }
}
