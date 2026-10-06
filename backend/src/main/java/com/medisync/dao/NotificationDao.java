package com.medisync.dao;

import com.medisync.dto.NotificationResponse;
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
public class NotificationDao {

    private final JdbcTemplate jdbcTemplate;

    public NotificationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<NotificationResponse> rowMapper = (rs, rowNum) -> {
        NotificationResponse response = new NotificationResponse();
        response.setNotificationId(rs.getLong("notification_id"));
        response.setMessage(rs.getString("message"));
        response.setIsRead(rs.getBoolean("is_read"));
        
        Timestamp t = rs.getTimestamp("created_at");
        if (t != null) {
            response.setCreatedAt(t.toLocalDateTime());
        }
        return response;
    };

    public Long create(Long userId, String message) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO Notification (user_id, message, is_read) VALUES (?, ?, false)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, userId);
            ps.setString(2, message);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public List<NotificationResponse> findAllByUserId(Long userId) {
        String sql = "SELECT * FROM Notification WHERE user_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    public List<NotificationResponse> findUnreadByUserId(Long userId) {
        String sql = "SELECT * FROM Notification WHERE user_id = ? AND is_read = false ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    public int markAsRead(Long notificationId, Long userId) {
        return jdbcTemplate.update(
                "UPDATE Notification SET is_read = true WHERE notification_id = ? AND user_id = ?",
                notificationId, userId
        );
    }

    public int markAllAsRead(Long userId) {
        return jdbcTemplate.update(
                "UPDATE Notification SET is_read = true WHERE user_id = ? AND is_read = false",
                userId
        );
    }
}
