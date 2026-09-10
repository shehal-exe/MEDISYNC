package com.medisync.dao;

import com.medisync.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        return user;
    };

    public User findByEmail(String email) {
        List<User> users = jdbcTemplate.query(
                "SELECT * FROM User WHERE email = ?",
                userRowMapper,
                email
        );
        return users.isEmpty() ? null : users.get(0);
    }

    public User findById(Long userId) {
        List<User> users = jdbcTemplate.query(
                "SELECT * FROM User WHERE user_id = ?",
                userRowMapper,
                userId
        );
        return users.isEmpty() ? null : users.get(0);
    }

    public Long save(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO User (email, password_hash) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            return ps;
        }, keyHolder);
        
        return keyHolder.getKey().longValue();
    }

    public void updatePassword(Long userId, String passwordHash) {
        jdbcTemplate.update(
                "UPDATE User SET password_hash = ? WHERE user_id = ?",
                passwordHash, userId
        );
    }
}
