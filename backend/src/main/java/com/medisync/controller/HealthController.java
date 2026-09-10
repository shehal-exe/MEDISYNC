package com.medisync.controller;

import com.medisync.service.DatabaseHealthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    private final DatabaseHealthService databaseHealthService;

    public HealthController(DatabaseHealthService databaseHealthService) {
        this.databaseHealthService = databaseHealthService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        boolean isDbUp = databaseHealthService.isDatabaseUp();
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", isDbUp);
        
        if (isDbUp) {
            response.put("message", "MEDISYNC API is running");
            response.put("status", "UP");
            response.put("database", "UP");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "MEDISYNC API database connection failed");
            response.put("status", "DOWN");
            response.put("database", "DOWN");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }
}
