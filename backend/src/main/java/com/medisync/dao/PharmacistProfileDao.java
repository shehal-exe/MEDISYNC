package com.medisync.dao;

import com.medisync.dto.PharmacistProfileResponse;
import com.medisync.dto.UpdatePharmacistProfileRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PharmacistProfileDao {

    private final JdbcTemplate jdbcTemplate;

    public PharmacistProfileDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<PharmacistProfileResponse> rowMapper = (rs, rowNum) -> {
        PharmacistProfileResponse profile = new PharmacistProfileResponse();
        profile.setPharmacistId(rs.getLong("pharmacist_id"));
        profile.setUserId(rs.getLong("user_id"));
        profile.setFirstName(rs.getString("first_name"));
        profile.setLastName(rs.getString("last_name"));
        profile.setLicenseNumber(rs.getString("license_number"));
        profile.setContactNumber(rs.getString("contact_number"));
        
        java.sql.Date hd = rs.getDate("hire_date");
        if (hd != null) {
            profile.setHireDate(hd.toLocalDate());
        }
        
        profile.setEmail(rs.getString("email"));
        return profile;
    };

    public PharmacistProfileResponse getProfileByUserId(Long userId) {
        String sql = "SELECT p.*, u.email FROM PharmacistProfile p " +
                     "JOIN User u ON p.user_id = u.user_id " +
                     "WHERE p.user_id = ?";
        List<PharmacistProfileResponse> results = jdbcTemplate.query(sql, rowMapper, userId);
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean updateProfile(Long userId, UpdatePharmacistProfileRequest request) {
        String sql = "UPDATE PharmacistProfile SET first_name = ?, last_name = ?, contact_number = ? WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
                request.getFirstName(),
                request.getLastName(),
                request.getContactNumber(),
                userId
        );
        return rowsAffected > 0;
    }
}
