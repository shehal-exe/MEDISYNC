package com.medisync.dao;

import com.medisync.dto.PrescriptionResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class PrescriptionDao {

    private final JdbcTemplate jdbcTemplate;

    public PrescriptionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<PrescriptionResponse> rowMapper = (rs, rowNum) -> {
        PrescriptionResponse response = new PrescriptionResponse();
        response.setPrescriptionId(rs.getLong("prescription_id"));
        
        Timestamp t = rs.getTimestamp("upload_date");
        if (t != null) response.setUploadDate(t.toLocalDateTime());
        
        response.setFilePath(rs.getString("file_path"));
        response.setStatus(rs.getString("status"));
        response.setNotes(rs.getString("notes"));
        return response;
    };

    public Long create(Long patientId, String filePath, String notes) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO Prescription (patient_id, file_path, notes, status) VALUES (?, ?, ?, 'PENDING')",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, patientId);
            ps.setString(2, filePath);
            ps.setString(3, notes != null ? notes : "");
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public List<PrescriptionResponse> findAllByPatientId(Long patientId) {
        String sql = "SELECT * FROM Prescription WHERE patient_id = ? ORDER BY upload_date DESC";
        return jdbcTemplate.query(sql, rowMapper, patientId);
    }

    public PrescriptionResponse findByIdAndPatientId(Long prescriptionId, Long patientId) {
        String sql = "SELECT * FROM Prescription WHERE prescription_id = ? AND patient_id = ?";
        List<PrescriptionResponse> results = jdbcTemplate.query(sql, rowMapper, prescriptionId, patientId);
        return results.isEmpty() ? null : results.get(0);
    }
}
