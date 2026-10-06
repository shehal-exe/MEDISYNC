package com.medisync.dao;

import com.medisync.dto.PatientMedicineResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class PatientMedicineDao {

    private final JdbcTemplate jdbcTemplate;

    public PatientMedicineDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<PatientMedicineResponse> rowMapper = (rs, rowNum) -> {
        PatientMedicineResponse pm = new PatientMedicineResponse();
        pm.setPatientMedicineId(rs.getLong("patient_medicine_id"));
        pm.setPatientId(rs.getLong("patient_id"));
        pm.setMedicineId(rs.getLong("medicine_id"));
        pm.setMedicineName(rs.getString("medicine_name"));
        pm.setManufacturer(rs.getString("manufacturer"));
        pm.setPrice(rs.getBigDecimal("price"));
        pm.setStockQuantity(rs.getInt("stock_quantity"));
        pm.setDosage(rs.getString("dosage"));
        pm.setInstructions(rs.getString("instructions"));
        pm.setIsActive(rs.getBoolean("is_active"));
        java.sql.Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) pm.setCreatedAt(ca.toLocalDateTime());
        return pm;
    };

    public List<PatientMedicineResponse> findAllByPatientId(Long patientId) {
        String sql = "SELECT pm.*, m.name AS medicine_name, m.manufacturer, " +
                     "COALESCE(SUM(ib.quantity_in_stock), 0) AS stock_quantity, " +
                     "COALESCE(MAX(ib.unit_price), 0) AS price " +
                     "FROM PatientMedicine pm " +
                     "JOIN Medicine m ON pm.medicine_id = m.medicine_id " +
                     "LEFT JOIN InventoryBatch ib ON m.medicine_id = ib.medicine_id " +
                     "WHERE pm.patient_id = ? " +
                     "GROUP BY pm.patient_medicine_id, pm.patient_id, pm.medicine_id, pm.dosage, pm.instructions, pm.is_active, pm.created_at, m.name, m.manufacturer " +
                     "ORDER BY pm.created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, patientId);
    }

    public PatientMedicineResponse findByIdAndPatientId(Long patientMedicineId, Long patientId) {
        String sql = "SELECT pm.*, m.name AS medicine_name, m.manufacturer, " +
                     "COALESCE(SUM(ib.quantity_in_stock), 0) AS stock_quantity, " +
                     "COALESCE(MAX(ib.unit_price), 0) AS price " +
                     "FROM PatientMedicine pm " +
                     "JOIN Medicine m ON pm.medicine_id = m.medicine_id " +
                     "LEFT JOIN InventoryBatch ib ON m.medicine_id = ib.medicine_id " +
                     "WHERE pm.patient_medicine_id = ? AND pm.patient_id = ? " +
                     "GROUP BY pm.patient_medicine_id, pm.patient_id, pm.medicine_id, pm.dosage, pm.instructions, pm.is_active, pm.created_at, m.name, m.manufacturer";
        List<PatientMedicineResponse> results = jdbcTemplate.query(sql, rowMapper, patientMedicineId, patientId);
        return results.isEmpty() ? null : results.get(0);
    }

    public Long create(Long patientId, Long medicineId, String dosage, String instructions) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO PatientMedicine (patient_id, medicine_id, dosage, instructions) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, patientId);
            ps.setLong(2, medicineId);
            ps.setString(3, dosage != null ? dosage : "");
            ps.setString(4, instructions != null ? instructions : "");
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public int update(Long patientMedicineId, Long patientId, String dosage, String instructions, Boolean isActive) {
        return jdbcTemplate.update(
                "UPDATE PatientMedicine SET dosage = ?, instructions = ?, is_active = ? " +
                "WHERE patient_medicine_id = ? AND patient_id = ?",
                dosage, instructions, isActive, patientMedicineId, patientId
        );
    }

    public int delete(Long patientMedicineId, Long patientId) {
        return jdbcTemplate.update(
                "DELETE FROM PatientMedicine WHERE patient_medicine_id = ? AND patient_id = ?",
                patientMedicineId, patientId
        );
    }
}
