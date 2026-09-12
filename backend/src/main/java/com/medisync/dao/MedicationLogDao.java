package com.medisync.dao;

import com.medisync.dto.MedicationLogResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class MedicationLogDao {

    private final JdbcTemplate jdbcTemplate;

    public MedicationLogDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<MedicationLogResponse> rowMapper = (rs, rowNum) -> {
        MedicationLogResponse response = new MedicationLogResponse();
        response.setLogId(rs.getLong("log_id"));
        response.setScheduleId(rs.getLong("schedule_id"));
        response.setMedicineName(rs.getString("medicine_name"));
        response.setStatus(rs.getString("status"));
        
        Timestamp t = rs.getTimestamp("log_time");
        if (t != null) {
            response.setLogTime(t.toLocalDateTime());
        }
        return response;
    };

    public Long createLog(Long scheduleId, String status, LocalDateTime logTime) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO MedicationLog (schedule_id, log_time, status) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, scheduleId);
            ps.setTimestamp(2, Timestamp.valueOf(logTime));
            ps.setString(3, status);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public List<MedicationLogResponse> findLogsByPatientId(Long patientId) {
        String sql = "SELECT ml.*, m.name as medicine_name FROM MedicationLog ml " +
                     "JOIN MedicationSchedule ms ON ml.schedule_id = ms.schedule_id " +
                     "JOIN PatientMedicine pm ON ms.patient_medicine_id = pm.patient_medicine_id " +
                     "JOIN Medicine m ON pm.medicine_id = m.medicine_id " +
                     "WHERE pm.patient_id = ? " +
                     "ORDER BY ml.log_time DESC";
        return jdbcTemplate.query(sql, rowMapper, patientId);
    }

    public boolean hasLogForScheduleAndDate(Long scheduleId, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM MedicationLog " +
                     "WHERE schedule_id = ? AND DATE(log_time) = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, scheduleId, java.sql.Date.valueOf(date));
        return count != null && count > 0;
    }
}
