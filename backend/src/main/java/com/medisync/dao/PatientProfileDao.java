package com.medisync.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public class PatientProfileDao {

    private final JdbcTemplate jdbcTemplate;

    public PatientProfileDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createProfile(Long userId, String firstName, String lastName, String contactNumber, String dateOfBirth) {
        Object dobParam = (dateOfBirth != null && !dateOfBirth.trim().isEmpty()) ? java.sql.Date.valueOf(LocalDate.parse(dateOfBirth)) : null;
        
        jdbcTemplate.update(
                "INSERT INTO PatientProfile (user_id, first_name, last_name, contact_number, date_of_birth) VALUES (?, ?, ?, ?, ?)",
                userId, firstName, lastName, contactNumber, dobParam
        );
    }

    public com.medisync.dto.PatientProfileResponse getProfileByUserId(Long userId) {
        String sql = "SELECT p.patient_id, p.user_id, u.email, p.first_name, p.last_name, p.date_of_birth, p.contact_number " +
                     "FROM PatientProfile p JOIN User u ON p.user_id = u.user_id WHERE p.user_id = ?";
        java.util.List<com.medisync.dto.PatientProfileResponse> profiles = jdbcTemplate.query(sql, (rs, rowNum) -> {
            com.medisync.dto.PatientProfileResponse profile = new com.medisync.dto.PatientProfileResponse();
            profile.setPatientId(rs.getLong("patient_id"));
            profile.setUserId(rs.getLong("user_id"));
            profile.setEmail(rs.getString("email"));
            profile.setFirstName(rs.getString("first_name"));
            profile.setLastName(rs.getString("last_name"));
            java.sql.Date dob = rs.getDate("date_of_birth");
            if (dob != null) profile.setDateOfBirth(dob.toLocalDate());
            profile.setContactNumber(rs.getString("contact_number"));
            return profile;
        }, userId);
        return profiles.isEmpty() ? null : profiles.get(0);
    }

    public void updateProfile(Long userId, String firstName, String lastName, java.time.LocalDate dateOfBirth, String contactNumber) {
        jdbcTemplate.update(
                "UPDATE PatientProfile SET first_name = ?, last_name = ?, date_of_birth = ?, contact_number = ? WHERE user_id = ?",
                firstName, lastName, dateOfBirth != null ? java.sql.Date.valueOf(dateOfBirth) : null, contactNumber, userId
        );
    }
}
