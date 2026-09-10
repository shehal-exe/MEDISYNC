package com.medisync.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MedicineDao {

    private final JdbcTemplate jdbcTemplate;

    public MedicineDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsById(Long medicineId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Medicine WHERE medicine_id = ?",
                Integer.class,
                medicineId
        );
        return count != null && count > 0;
    }
}
