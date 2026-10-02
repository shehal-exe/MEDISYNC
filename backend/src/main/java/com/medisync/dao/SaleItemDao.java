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

    public void create(Long pharmacistId, Long saleId, Long medicineId, int quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
        Long batchId = jdbcTemplate.queryForObject(
                "SELECT ib.batch_id FROM InventoryBatch ib " +
                "JOIN Medicine m ON ib.medicine_id = m.medicine_id " +
                "WHERE m.pharmacist_id = ? AND ib.medicine_id = ? AND ib.quantity_in_stock >= ? " +
                "ORDER BY ib.expiry_date ASC LIMIT 1",
                Long.class,
                pharmacistId,
                medicineId,
                quantity
        );

        jdbcTemplate.update(
                "UPDATE InventoryBatch SET quantity_in_stock = quantity_in_stock - ? WHERE batch_id = ?",
                quantity,
                batchId
        );

        jdbcTemplate.update(
                "INSERT INTO SaleItem (sale_id, batch_id, quantity, price_at_sale) VALUES (?, ?, ?, ?)",
                saleId, batchId, quantity, unitPrice
        );
    }

    public List<SaleItemResponse> findBySaleId(Long saleId) {
        String sql = "SELECT si.sale_item_id, ib.medicine_id, m.name as medicine_name, " +
                     "si.quantity, si.price_at_sale as unit_price, " +
                     "(si.price_at_sale * si.quantity) as total_price " +
                     "FROM SaleItem si " +
                     "JOIN InventoryBatch ib ON si.batch_id = ib.batch_id " +
                     "JOIN Medicine m ON ib.medicine_id = m.medicine_id " +
                     "WHERE si.sale_id = ?";
        return jdbcTemplate.query(sql, rowMapper, saleId);
    }
}
