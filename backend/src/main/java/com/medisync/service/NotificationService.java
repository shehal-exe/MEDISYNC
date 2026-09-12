package com.medisync.service;

import com.medisync.dao.NotificationDao;
import com.medisync.dao.UserDao;
import com.medisync.dto.NotificationResponse;
import com.medisync.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationDao notificationDao;
    private final UserDao userDao;

    public NotificationService(NotificationDao notificationDao, UserDao userDao) {
        this.notificationDao = notificationDao;
        this.userDao = userDao;
    }

    private Long getUserIdByEmail(String email) {
        User user = userDao.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return user.getUserId();
    }

    public List<NotificationResponse> getMyNotifications(String email) {
        Long userId = getUserIdByEmail(email);
        return notificationDao.findAllByUserId(userId);
    }

    public List<NotificationResponse> getMyUnreadNotifications(String email) {
        Long userId = getUserIdByEmail(email);
        return notificationDao.findUnreadByUserId(userId);
    }

    public void markAsRead(String email, Long notificationId) {
        Long userId = getUserIdByEmail(email);
        int rows = notificationDao.markAsRead(notificationId, userId);
        if (rows == 0) {
            throw new IllegalArgumentException("Notification not found or access denied");
        }
    }

    public void markAllAsRead(String email) {
        Long userId = getUserIdByEmail(email);
        notificationDao.markAllAsRead(userId);
    }
    
    // Internal method to be called by other services (e.g. Pharmacist verifying prescription)
    public void createNotification(Long userId, String message) {
        notificationDao.create(userId, message);
    }
}
