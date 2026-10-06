package com.medisync.controller;

import com.medisync.dto.ApiResponse;
import com.medisync.dto.CreateMedicationScheduleRequest;
import com.medisync.dto.MedicationScheduleResponse;
import com.medisync.dto.UpdateMedicationScheduleRequest;
import com.medisync.service.MedicationScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patient/medication-schedules")
public class MedicationScheduleController {

    private final MedicationScheduleService medicationScheduleService;

    public MedicationScheduleController(MedicationScheduleService medicationScheduleService) {
        this.medicationScheduleService = medicationScheduleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicationScheduleResponse>>> getSchedules(Authentication authentication) {
        List<MedicationScheduleResponse> schedules = medicationScheduleService.getSchedules(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Schedules retrieved successfully", schedules));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicationScheduleResponse>> getScheduleById(@PathVariable Long id, Authentication authentication) {
        try {
            MedicationScheduleResponse response = medicationScheduleService.getScheduleById(authentication.getName(), id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Schedule retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, e.getMessage(), "NOT_FOUND"));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MedicationScheduleResponse>> createSchedule(@Valid @RequestBody CreateMedicationScheduleRequest request, Authentication authentication) {
        try {
            MedicationScheduleResponse response = medicationScheduleService.createSchedule(authentication.getName(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Schedule created successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, e.getMessage(), "BAD_REQUEST"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicationScheduleResponse>> updateSchedule(@PathVariable Long id, @Valid @RequestBody UpdateMedicationScheduleRequest request, Authentication authentication) {
        try {
            MedicationScheduleResponse response = medicationScheduleService.updateSchedule(authentication.getName(), id, request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Schedule updated successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, e.getMessage(), "BAD_REQUEST"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable Long id, Authentication authentication) {
        try {
            medicationScheduleService.deleteSchedule(authentication.getName(), id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Schedule deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, e.getMessage(), "NOT_FOUND"));
        }
    }
}
