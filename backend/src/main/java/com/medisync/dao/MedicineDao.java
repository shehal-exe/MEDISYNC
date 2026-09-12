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

    public boolean existsById(Long medicineId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Medicine WHERE medicine_id = ?",
                Integer.class,
                medicineId
        );
        return count != null && count > 0;
    }

    public Long create(MedicineRequest req) {
        String sql = "INSERT INTO Medicine (name, description, manufacturer) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, req.getName());
            ps.setString(2, req.getDescription());
            ps.setString(3, req.getManufacturer());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public List<MedicineResponse> findAll() {
        String sql = "SELECT m.medicine_id, m.name, m.description, m.manufacturer, " +
                     "SUM(ib.quantity_in_stock) as stock_quantity, MAX(ib.unit_price) as price " +
                     "FROM Medicine m " +
                     "LEFT JOIN InventoryBatch ib ON m.medicine_id = ib.medicine_id " +
                     "GROUP BY m.medicine_id, m.name, m.description, m.manufacturer " +
                     "ORDER BY m.name ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public MedicineResponse findById(Long id) {
        String sql = "SELECT m.medicine_id, m.name, m.description, m.manufacturer, " +
                     "SUM(ib.quantity_in_stock) as stock_quantity, MAX(ib.unit_price) as price " +
                     "FROM Medicine m " +
                     "LEFT JOIN InventoryBatch ib ON m.medicine_id = ib.medicine_id " +
                     "WHERE m.medicine_id = ? " +
                     "GROUP BY m.medicine_id, m.name, m.description, m.manufacturer";
        List<MedicineResponse> results = jdbcTemplate.query(sql, rowMapper, id);
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean update(Long id, MedicineRequest req) {
        String sql = "UPDATE Medicine SET name=?, description=?, manufacturer=? WHERE medicine_id=?";
        int rows = jdbcTemplate.update(sql,
                req.getName(),
                req.getDescription(),
                req.getManufacturer(),
                id);
        return rows > 0;
    }

    public boolean delete(Long id) {
        return jdbcTemplate.update("DELETE FROM Medicine WHERE medicine_id = ?", id) > 0;
    }
}
