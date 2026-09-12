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
        m.setPrice(rs.getBigDecimal("price"));
        m.setStockQuantity(rs.getInt("stock_quantity"));
        m.setRequiresPrescription(rs.getBoolean("requires_prescription"));
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
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO Medicine (name, description, manufacturer, price, stock_quantity, requires_prescription) " +
                    "VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, req.getName());
            ps.setString(2, req.getDescription());
            ps.setString(3, req.getManufacturer());
            ps.setBigDecimal(4, req.getPrice());
            ps.setInt(5, req.getStockQuantity());
            ps.setBoolean(6, req.getRequiresPrescription() != null ? req.getRequiresPrescription() : false);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public List<MedicineResponse> findAll() {
        return jdbcTemplate.query("SELECT * FROM Medicine ORDER BY name ASC", rowMapper);
    }

    public MedicineResponse findById(Long id) {
        List<MedicineResponse> results = jdbcTemplate.query("SELECT * FROM Medicine WHERE medicine_id = ?", rowMapper, id);
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean update(Long id, MedicineRequest req) {
        String sql = "UPDATE Medicine SET name=?, description=?, manufacturer=?, price=?, stock_quantity=?, requires_prescription=? WHERE medicine_id=?";
        int rows = jdbcTemplate.update(sql,
                req.getName(),
                req.getDescription(),
                req.getManufacturer(),
                req.getPrice(),
                req.getStockQuantity(),
                req.getRequiresPrescription() != null ? req.getRequiresPrescription() : false,
                id);
        return rows > 0;
    }

    public boolean delete(Long id) {
        return jdbcTemplate.update("DELETE FROM Medicine WHERE medicine_id = ?", id) > 0;
    }
}
