package com.medisync.dao;

import com.medisync.dto.MedicineRequest;
import com.medisync.dto.MedicineResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@Repository
public class MedicineDao {

    private final JdbcTemplate jdbcTemplate;

    public MedicineDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<MedicineResponse> rowMapper = (rs, rowNum) -> {
        MedicineResponse m = new MedicineResponse();
        m.setMedicineId(rs.getLong("medicine_id"));
        m.setName(rs.getString("name"));
        m.setDescription(rs.getString("description"));
        m.setManufacturer(rs.getString("manufacturer"));
        m.setPrice(rs.getBigDecimal("price") != null ? rs.getBigDecimal("price") : java.math.BigDecimal.ZERO);
        m.setStockQuantity(rs.getInt("stock_quantity"));
        m.setRequiresPrescription(false);
        return m;
    };

    public boolean existsById(Long pharmacistId, Long medicineId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Medicine WHERE pharmacist_id = ? AND medicine_id = ?",
                Integer.class,
                pharmacistId,
                medicineId
        );
        return count != null && count > 0;
    }

    public boolean existsById(Long medicineId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Medicine WHERE medicine_id = ?",
                Integer.class,
                medicineId
        );
        return count != null && count > 0;
    }

    public Long create(Long pharmacistId, MedicineRequest req) {
        String sql = "INSERT INTO Medicine (pharmacist_id, name, description, manufacturer) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, pharmacistId);
            ps.setString(2, req.getName());
            ps.setString(3, req.getDescription());
            ps.setString(4, req.getManufacturer());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void createInventoryBatch(Long medicineId, String batchNumber, LocalDate expiryDate,
                                     Integer quantityInStock, BigDecimal unitPrice) {
        String sql = "INSERT INTO InventoryBatch " +
                     "(medicine_id, batch_number, expiry_date, quantity_in_stock, unit_price) " +
                     "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, medicineId, batchNumber, expiryDate, quantityInStock, unitPrice);
    }

    public void updateInventorySnapshot(Long pharmacistId, Long medicineId, Integer stockQuantity, BigDecimal unitPrice) {
        List<Long> batchIds = jdbcTemplate.query(
                "SELECT ib.batch_id FROM InventoryBatch ib " +
                        "JOIN Medicine m ON ib.medicine_id = m.medicine_id " +
                        "WHERE m.pharmacist_id = ? AND ib.medicine_id = ? " +
                        "ORDER BY ib.expiry_date DESC, ib.batch_id DESC LIMIT 1",
                (rs, rowNum) -> rs.getLong("batch_id"),
                pharmacistId,
                medicineId
        );

        if (batchIds.isEmpty()) {
            createInventoryBatch(
                    medicineId,
                    "MANUAL-" + medicineId,
                    LocalDate.now().plusYears(2),
                    stockQuantity,
                    unitPrice
            );
            return;
        }

        Long primaryBatchId = batchIds.get(0);
        jdbcTemplate.update(
                "UPDATE InventoryBatch SET quantity_in_stock = 0, unit_price = ? WHERE medicine_id = ? AND batch_id <> ?",
                unitPrice,
                medicineId,
                primaryBatchId
        );
        jdbcTemplate.update(
                "UPDATE InventoryBatch SET quantity_in_stock = ?, unit_price = ? WHERE batch_id = ?",
                stockQuantity,
                unitPrice,
                primaryBatchId
        );
    }

    public List<MedicineResponse> findAll(Long pharmacistId) {
        String sql = "SELECT m.medicine_id, m.name, m.description, m.manufacturer, " +
                     "COALESCE(SUM(ib.quantity_in_stock), 0) as stock_quantity, MAX(ib.unit_price) as price " +
                     "FROM Medicine m " +
                     "LEFT JOIN InventoryBatch ib ON m.medicine_id = ib.medicine_id " +
                     "WHERE m.pharmacist_id = ? " +
                     "GROUP BY m.medicine_id, m.name, m.description, m.manufacturer " +
                     "ORDER BY m.name ASC";
        return jdbcTemplate.query(sql, rowMapper, pharmacistId);
    }

    public List<MedicineResponse> findCatalog() {
        String sql = "SELECT m.medicine_id, m.name, m.description, m.manufacturer, " +
                     "COALESCE(SUM(ib.quantity_in_stock), 0) as stock_quantity, MAX(ib.unit_price) as price " +
                     "FROM Medicine m " +
                     "LEFT JOIN InventoryBatch ib ON m.medicine_id = ib.medicine_id " +
                     "GROUP BY m.medicine_id, m.name, m.description, m.manufacturer " +
                     "ORDER BY m.name ASC, m.medicine_id ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public MedicineResponse findById(Long pharmacistId, Long id) {
        String sql = "SELECT m.medicine_id, m.name, m.description, m.manufacturer, " +
                     "COALESCE(SUM(ib.quantity_in_stock), 0) as stock_quantity, MAX(ib.unit_price) as price " +
                     "FROM Medicine m " +
                     "LEFT JOIN InventoryBatch ib ON m.medicine_id = ib.medicine_id " +
                     "WHERE m.pharmacist_id = ? AND m.medicine_id = ? " +
                     "GROUP BY m.medicine_id, m.name, m.description, m.manufacturer";
        List<MedicineResponse> results = jdbcTemplate.query(sql, rowMapper, pharmacistId, id);
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean update(Long pharmacistId, Long id, MedicineRequest req) {
        String sql = "UPDATE Medicine SET name=?, description=?, manufacturer=? WHERE pharmacist_id=? AND medicine_id=?";
        int rows = jdbcTemplate.update(sql,
                req.getName(),
                req.getDescription(),
                req.getManufacturer(),
                pharmacistId,
                id);
        return rows > 0;
    }

    public boolean delete(Long pharmacistId, Long id) {
        return jdbcTemplate.update("DELETE FROM Medicine WHERE pharmacist_id = ? AND medicine_id = ?", pharmacistId, id) > 0;
    }
}
