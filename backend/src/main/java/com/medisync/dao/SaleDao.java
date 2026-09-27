package com.medisync.dao;

import com.medisync.dto.SaleResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

@Repository
public class SaleDao {

    private final JdbcTemplate jdbcTemplate;

    public SaleDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SaleResponse> rowMapper = (rs, rowNum) -> {
        SaleResponse s = new SaleResponse();
        s.setSaleId(rs.getLong("sale_id"));
        s.setPatientId(rs.getObject("patient_id") != null ? rs.getLong("patient_id") : null);
        s.setPharmacistId(rs.getLong("pharmacist_id"));
        
        Timestamp t = rs.getTimestamp("sale_date");
        if (t != null) s.setSaleDate(t.toLocalDateTime());
        
        s.setTotalAmount(rs.getBigDecimal("total_amount"));
        s.setPaymentStatus("COMPLETED");
        return s;
    };

    public Long create(Long patientId, Long pharmacistId, BigDecimal totalAmount) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO Sale (patient_id, pharmacist_id, total_amount) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            if (patientId != null) {
                ps.setLong(1, patientId);
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setLong(2, pharmacistId);
            ps.setBigDecimal(3, totalAmount);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public List<SaleResponse> findAll() {
        return jdbcTemplate.query("SELECT * FROM Sale ORDER BY sale_date DESC", rowMapper);
    }

    public List<SaleResponse> findAllByPharmacistId(Long pharmacistId) {
        return jdbcTemplate.query(
                "SELECT * FROM Sale WHERE pharmacist_id = ? ORDER BY sale_date DESC",
                rowMapper,
                pharmacistId
        );
    }

    public SaleResponse findById(Long saleId) {
        List<SaleResponse> results = jdbcTemplate.query("SELECT * FROM Sale WHERE sale_id = ?", rowMapper, saleId);
        return results.isEmpty() ? null : results.get(0);
    }

    public SaleResponse findByIdAndPharmacistId(Long saleId, Long pharmacistId) {
        List<SaleResponse> results = jdbcTemplate.query(
                "SELECT * FROM Sale WHERE sale_id = ? AND pharmacist_id = ?",
                rowMapper,
                saleId,
                pharmacistId
        );
        return results.isEmpty() ? null : results.get(0);
    }
}
