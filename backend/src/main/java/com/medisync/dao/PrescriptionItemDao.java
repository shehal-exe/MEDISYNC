package com.medisync.dao;

import com.medisync.dto.PrescriptionItemResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PrescriptionItemDao {

    private final JdbcTemplate jdbcTemplate;

    public PrescriptionItemDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<PrescriptionItemResponse> rowMapper = (rs, rowNum) -> {
        PrescriptionItemResponse response = new PrescriptionItemResponse();
        response.setPrescriptionItemId(rs.getLong("prescription_item_id"));
        response.setMedicineId(rs.getLong("medicine_id"));
        response.setMedicineName(rs.getString("medicine_name"));
        response.setPrescribedQuantity(rs.getInt("prescribed_quantity"));
        response.setDosageInstructions(rs.getString("dosage_instructions"));
        return response;
    };

    public List<PrescriptionItemResponse> findAllByPrescriptionId(Long prescriptionId) {
        String sql = "SELECT pi.*, m.name as medicine_name FROM PrescriptionItem pi " +
                     "JOIN Medicine m ON pi.medicine_id = m.medicine_id " +
                     "WHERE pi.prescription_id = ?";
        return jdbcTemplate.query(sql, rowMapper, prescriptionId);
    }
}
