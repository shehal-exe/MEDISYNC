package com.medisync.controller;

import com.medisync.dto.ApiResponse;
import com.medisync.dto.NotificationResponse;
import com.medisync.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(Authentication authentication) {
        List<NotificationResponse> notifications = notificationService.getMyNotifications(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Notifications retrieved successfully", notifications));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyUnreadNotifications(Authentication authentication) {
        List<NotificationResponse> notifications = notificationService.getMyUnreadNotifications(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Unread notifications retrieved successfully", notifications));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id, Authentication authentication) {
        try {
            notificationService.markAsRead(authentication.getName(), id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Notification marked as read"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, e.getMessage(), "NOT_FOUND"));
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication authentication) {
        notificationService.markAllAsRead(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "All notifications marked as read"));
    }
}
