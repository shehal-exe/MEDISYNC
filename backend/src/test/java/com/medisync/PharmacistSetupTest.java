package com.medisync;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class PharmacistSetupTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void setupPharmacist() {
        String email = "pharmacist_real@medisync.com";
        String password = "password123";
        String hash = passwordEncoder.encode(password);
        
        // Insert User
        jdbcTemplate.update("INSERT INTO User (email, password_hash) VALUES (?, ?)", email, hash);
        
        // Get user_id
        Long userId = jdbcTemplate.queryForObject("SELECT user_id FROM User WHERE email = ?", Long.class, email);
        
        // Insert Role
        jdbcTemplate.update("INSERT INTO Role (user_id, role_name) VALUES (?, 'PHARMACIST')", userId);
        
        // Insert Pharmacist Profile
        jdbcTemplate.update("INSERT INTO PharmacistProfile (user_id, first_name, last_name, license_number) VALUES (?, 'Dr. Admin', 'Smith', 'PH-99999')", userId);
        
        System.out.println("PHARMACIST CREATED! Email: " + email + ", Password: " + password);
    }
}
