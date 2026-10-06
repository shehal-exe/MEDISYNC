package com.medisync.dao;

import com.medisync.dto.CreateRefillRequest;
import com.medisync.dto.RefillRequestResponse;
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
public class RefillRequestDao {

    private final JdbcTemplate jdbcTemplate;

    public RefillRequestDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<RefillRequestResponse> rowMapper = (rs, rowNum) -> {
        RefillRequestResponse response = new RefillRequestResponse();
        response.setRefillRequestId(rs.getLong("refill_request_id"));
        response.setPatientId(rs.getLong("patient_id"));
        response.setPatientName((rs.getString("first_name") + " " + rs.getString("last_name")).trim());
        response.setPatientEmail(rs.getString("email"));
        response.setMedicineName(rs.getString("medicine_name"));
        response.setQuantity(rs.getInt("quantity"));
        response.setFulfillmentMethod(rs.getString("fulfillment_method"));
        response.setEstimatedTotal(rs.getBigDecimal("estimated_total"));
        response.setStatus(rs.getString("status"));
        response.setPharmacistNotes(rs.getString("pharmacist_notes"));
        response.setApprovedBy(rs.getObject("approved_by") != null ? rs.getLong("approved_by") : null);
        response.setRequestedPharmacistId(rs.getObject("requested_pharmacist_id") != null ? rs.getLong("requested_pharmacist_id") : null);
        String pharmacistFirstName = rs.getString("pharmacist_first_name");
        String pharmacistLastName = rs.getString("pharmacist_last_name");
        if (pharmacistFirstName != null || pharmacistLastName != null) {
            response.setRequestedPharmacistName(((pharmacistFirstName != null ? pharmacistFirstName : "") + " " +
                    (pharmacistLastName != null ? pharmacistLastName : "")).trim());
        }
        response.setRequestedPharmacistHandle(rs.getString("pharmacist_handle"));

        Timestamp requestedAt = rs.getTimestamp("requested_at");
        if (requestedAt != null) response.setRequestedAt(requestedAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) response.setUpdatedAt(updatedAt.toLocalDateTime());

        return response;
    };

    public Long create(Long patientId, CreateRefillRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO RefillRequest " +
                            "(patient_id, requested_pharmacist_id, medicine_name, quantity, fulfillment_method, estimated_total, status) " +
                            "VALUES (?, ?, ?, ?, ?, ?, 'PENDING')",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, patientId);
            ps.setLong(2, request.getPharmacistId());
            ps.setString(3, request.getMedicineName());
            ps.setInt(4, request.getQuantity());
            ps.setString(5, request.getFulfillmentMethod());
            ps.setBigDecimal(6, request.getEstimatedTotal());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public List<RefillRequestResponse> findAll() {
        return jdbcTemplate.query(baseSelect() + " ORDER BY rr.requested_at DESC", rowMapper);
    }

    public List<RefillRequestResponse> findByStatus(String status) {
        return jdbcTemplate.query(baseSelect() + " WHERE rr.status = ? ORDER BY rr.requested_at DESC", rowMapper, status);
    }

    public List<RefillRequestResponse> findAllByRequestedPharmacistId(Long pharmacistId) {
        return jdbcTemplate.query(
                baseSelect() + " WHERE rr.requested_pharmacist_id = ? ORDER BY rr.requested_at DESC",
                rowMapper,
                pharmacistId
        );
    }

    public List<RefillRequestResponse> findByRequestedPharmacistIdAndStatus(Long pharmacistId, String status) {
        return jdbcTemplate.query(
                baseSelect() + " WHERE rr.requested_pharmacist_id = ? AND rr.status = ? ORDER BY rr.requested_at DESC",
                rowMapper,
                pharmacistId,
                status
        );
    }

    public List<RefillRequestResponse> findAllByPatientId(Long patientId) {
        return jdbcTemplate.query(baseSelect() + " WHERE rr.patient_id = ? ORDER BY rr.requested_at DESC", rowMapper, patientId);
    }

    public RefillRequestResponse findById(Long refillRequestId) {
        List<RefillRequestResponse> results = jdbcTemplate.query(
                baseSelect() + " WHERE rr.refill_request_id = ?",
                rowMapper,
                refillRequestId
        );
        return results.isEmpty() ? null : results.get(0);
    }

    public int updateStatus(Long refillRequestId, Long pharmacistId, String status, String notes) {
        return jdbcTemplate.update(
                "UPDATE RefillRequest SET approved_by = ?, status = ?, pharmacist_notes = ? WHERE refill_request_id = ?",
                pharmacistId,
                status,
                notes,
                refillRequestId
        );
    }

    private String baseSelect() {
        return "SELECT rr.*, pp.first_name, pp.last_name, u.email, " +
                "rp.first_name AS pharmacist_first_name, rp.last_name AS pharmacist_last_name, " +
                "rp.public_handle AS pharmacist_handle " +
                "FROM RefillRequest rr " +
                "JOIN PatientProfile pp ON rr.patient_id = pp.patient_id " +
                "JOIN User u ON pp.user_id = u.user_id " +
                "LEFT JOIN PharmacistProfile rp ON rr.requested_pharmacist_id = rp.pharmacist_id";
    }
}
