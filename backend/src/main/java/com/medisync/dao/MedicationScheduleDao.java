package com.medisync.dao;

import com.medisync.dto.MedicationScheduleResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public class MedicationScheduleDao {

    private final JdbcTemplate jdbcTemplate;

    public MedicationScheduleDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<MedicationScheduleResponse> rowMapper = (rs, rowNum) -> {
        MedicationScheduleResponse response = new MedicationScheduleResponse();
        response.setScheduleId(rs.getLong("schedule_id"));
        response.setPatientMedicineId(rs.getLong("patient_medicine_id"));
        response.setMedicineName(rs.getString("medicine_name"));
        response.setFrequency(rs.getString("frequency"));
        
        Time t = rs.getTime("time_of_day");
        if (t != null) {
            response.setTimeOfDay(t.toLocalTime());
        }
        
        Date sd = rs.getDate("start_date");
        if (sd != null) {
            response.setStartDate(sd.toLocalDate());
        }
        
        Date ed = rs.getDate("end_date");
        if (ed != null) {
            response.setEndDate(ed.toLocalDate());
        }
        return response;
    };

    public List<MedicationScheduleResponse> findAllByPatientId(Long patientId) {
        String sql = "SELECT ms.*, m.name as medicine_name FROM MedicationSchedule ms " +
                     "JOIN PatientMedicine pm ON ms.patient_medicine_id = pm.patient_medicine_id " +
                     "JOIN Medicine m ON pm.medicine_id = m.medicine_id " +
                     "WHERE pm.patient_id = ?";
        return jdbcTemplate.query(sql, rowMapper, patientId);
    }

    public MedicationScheduleResponse findByIdAndPatientId(Long scheduleId, Long patientId) {
        String sql = "SELECT ms.*, m.name as medicine_name FROM MedicationSchedule ms " +
                     "JOIN PatientMedicine pm ON ms.patient_medicine_id = pm.patient_medicine_id " +
                     "JOIN Medicine m ON pm.medicine_id = m.medicine_id " +
                     "WHERE ms.schedule_id = ? AND pm.patient_id = ?";
        List<MedicationScheduleResponse> results = jdbcTemplate.query(sql, rowMapper, scheduleId, patientId);
        return results.isEmpty() ? null : results.get(0);
    }

    public Long create(Long patientMedicineId, String frequency, LocalTime timeOfDay, LocalDate startDate, LocalDate endDate) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO MedicationSchedule (patient_medicine_id, frequency, time_of_day, start_date, end_date) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, patientMedicineId);
            ps.setString(2, frequency);
            ps.setTime(3, Time.valueOf(timeOfDay));
            ps.setDate(4, Date.valueOf(startDate));
            if (endDate != null) {
                ps.setDate(5, Date.valueOf(endDate));
            } else {
                ps.setNull(5, java.sql.Types.DATE);
            }
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public int update(Long scheduleId, String frequency, LocalTime timeOfDay, LocalDate startDate, LocalDate endDate) {
        return jdbcTemplate.update(
                "UPDATE MedicationSchedule SET frequency = ?, time_of_day = ?, start_date = ?, end_date = ? " +
                "WHERE schedule_id = ?",
                frequency, Time.valueOf(timeOfDay), Date.valueOf(startDate), endDate != null ? Date.valueOf(endDate) : null, scheduleId
        );
    }

    public int delete(Long scheduleId) {
        return jdbcTemplate.update("DELETE FROM MedicationSchedule WHERE schedule_id = ?", scheduleId);
    }
}
