package com.medisync.controller;

import com.medisync.dto.AdherenceResponse;
import com.medisync.dto.ApiResponse;
import com.medisync.dto.MedicationLogResponse;
import com.medisync.dto.ReminderResponse;
import com.medisync.service.MedicationReminderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patient")
public class MedicationReminderController {

    private final MedicationReminderService reminderService;

    public MedicationReminderController(MedicationReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping("/reminders")
    public ResponseEntity<ApiResponse<List<ReminderResponse>>> getReminders(Authentication authentication) {
        List<ReminderResponse> reminders = reminderService.getUpcomingReminders(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Reminders retrieved successfully", reminders));
    }

    @PostMapping("/reminders/{id}/taken")
    public ResponseEntity<ApiResponse<Void>> markTaken(@PathVariable Long id, Authentication authentication) {
        try {
            reminderService.markReminder(authentication.getName(), id, "TAKEN");
            return ResponseEntity.ok(new ApiResponse<>(true, "Marked as TAKEN successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, e.getMessage(), "BAD_REQUEST"));
        }
    }

    @PostMapping("/reminders/{id}/skipped")
    public ResponseEntity<ApiResponse<Void>> markSkipped(@PathVariable Long id, Authentication authentication) {
        try {
            reminderService.markReminder(authentication.getName(), id, "SKIPPED");
            return ResponseEntity.ok(new ApiResponse<>(true, "Marked as SKIPPED successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, e.getMessage(), "BAD_REQUEST"));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<MedicationLogResponse>>> getHistory(Authentication authentication) {
        List<MedicationLogResponse> history = reminderService.getHistory(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "History retrieved successfully", history));
    }

    @GetMapping("/adherence")
    public ResponseEntity<ApiResponse<AdherenceResponse>> getAdherence(Authentication authentication) {
        AdherenceResponse adherence = reminderService.getAdherence(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Adherence retrieved successfully", adherence));
    }
}
