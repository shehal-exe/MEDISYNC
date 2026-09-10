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
}
