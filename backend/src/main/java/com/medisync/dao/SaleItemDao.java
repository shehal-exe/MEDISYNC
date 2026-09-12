package com.medisync.dao;

import com.medisync.dto.SaleItemResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class SaleItemDao {

    private final JdbcTemplate jdbcTemplate;

    public SaleItemDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SaleItemResponse> rowMapper = (rs, rowNum) -> {
        SaleItemResponse item = new SaleItemResponse();
        item.setSaleItemId(rs.getLong("sale_item_id"));
        item.setMedicineId(rs.getLong("medicine_id"));
        item.setMedicineName(rs.getString("medicine_name"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setTotalPrice(rs.getBigDecimal("total_price"));
        return item;
    };

    public void create(Long saleId, Long medicineId, int quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
        jdbcTemplate.update(
                "INSERT INTO SaleItem (sale_id, medicine_id, quantity, unit_price, total_price) VALUES (?, ?, ?, ?, ?)",
                saleId, medicineId, quantity, unitPrice, totalPrice
        );
    }

    public List<SaleItemResponse> findBySaleId(Long saleId) {
        String sql = "SELECT si.*, m.name as medicine_name FROM SaleItem si " +
                     "JOIN Medicine m ON si.medicine_id = m.medicine_id " +
                     "WHERE si.sale_id = ?";
        return jdbcTemplate.query(sql, rowMapper, saleId);
    }
}
