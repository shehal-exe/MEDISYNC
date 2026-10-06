package com.medisync.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class PasswordResetTokenDao {

    private final JdbcTemplate jdbcTemplate;

    public PasswordResetTokenDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createToken(Long userId, String tokenHash, LocalDateTime expiryDate) {
        jdbcTemplate.update(
                "INSERT INTO PasswordResetToken (user_id, token_hash, expiry_date) VALUES (?, ?, ?)",
                userId, tokenHash, java.sql.Timestamp.valueOf(expiryDate)
        );
    }

    public Long getUserIdByValidTokenHash(String tokenHash, LocalDateTime currentTime) {
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT user_id, expiry_date FROM PasswordResetToken WHERE token_hash = ?",
                tokenHash
        );
        if (results.isEmpty()) return null;
        
        java.sql.Timestamp expiry = (java.sql.Timestamp) results.get(0).get("expiry_date");
        if (expiry.toLocalDateTime().isBefore(currentTime)) {
            return null; // expired
        }
        
        return (Long) results.get(0).get("user_id");
    }

    public void invalidateToken(String tokenHash) {
        jdbcTemplate.update("DELETE FROM PasswordResetToken WHERE token_hash = ?", tokenHash);
    }
}
