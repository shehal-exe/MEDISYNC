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
        profile.setPublicHandle(rs.getString("public_handle"));
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

    public PharmacistProfileResponse getProfileByPharmacistId(Long pharmacistId) {
        String sql = "SELECT p.*, u.email FROM PharmacistProfile p " +
                     "JOIN User u ON p.user_id = u.user_id " +
                     "WHERE p.pharmacist_id = ?";
        List<PharmacistProfileResponse> results = jdbcTemplate.query(sql, rowMapper, pharmacistId);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<PharmacistProfileResponse> findAllProfiles() {
        String sql = "SELECT p.*, u.email FROM PharmacistProfile p " +
                     "JOIN User u ON p.user_id = u.user_id " +
                     "ORDER BY p.first_name, p.last_name, p.public_handle";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Long getUserIdByPharmacistId(Long pharmacistId) {
        List<Long> results = jdbcTemplate.query(
                "SELECT user_id FROM PharmacistProfile WHERE pharmacist_id = ?",
                (rs, rowNum) -> rs.getLong("user_id"),
                pharmacistId
        );
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean isDisplayNameTaken(Long currentUserId, String firstName, String lastName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM PharmacistProfile " +
                        "WHERE LOWER(first_name) = LOWER(?) AND LOWER(last_name) = LOWER(?) AND user_id <> ?",
                Integer.class,
                firstName,
                lastName,
                currentUserId
        );
        return count != null && count > 0;
    }

    public boolean isLicenseNumberTaken(String licenseNumber) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM PharmacistProfile WHERE LOWER(license_number) = LOWER(?)",
                Integer.class,
                licenseNumber
        );
        return count != null && count > 0;
    }

    public boolean isPublicHandleTaken(String publicHandle) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM PharmacistProfile WHERE LOWER(public_handle) = LOWER(?)",
                Integer.class,
                publicHandle
        );
        return count != null && count > 0;
    }

    public void createProfile(Long userId, String firstName, String lastName, String licenseNumber, String publicHandle) {
        jdbcTemplate.update(
                "INSERT INTO PharmacistProfile (user_id, first_name, last_name, license_number, public_handle) VALUES (?, ?, ?, ?, ?)",
                userId,
                firstName,
                lastName,
                licenseNumber,
                publicHandle
        );
    }

    public boolean updateProfile(Long userId, UpdatePharmacistProfileRequest request) {
        String sql = "UPDATE PharmacistProfile SET first_name = ?, last_name = ? WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
                request.getFirstName(),
                request.getLastName(),
                userId
        );
        return rowsAffected > 0;
    }
}
