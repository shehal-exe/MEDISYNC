package com.medisync.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RoleDao {

    private final JdbcTemplate jdbcTemplate;

    public RoleDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addRole(Long userId, String roleName) {
        jdbcTemplate.update(
                "INSERT INTO Role (user_id, role_name) VALUES (?, ?)",
                userId, roleName
        );
    }

    public String getRoleByUserId(Long userId) {
        List<String> roles = jdbcTemplate.queryForList(
                "SELECT role_name FROM Role WHERE user_id = ?",
                String.class,
                userId
        );
        return roles.isEmpty() ? null : roles.get(0);
    }
}
