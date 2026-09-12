package com.medisync.controller;

import com.medisync.dto.ApiResponse;
import com.medisync.dto.PrescriptionResponse;
import com.medisync.service.PatientPrescriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patient/prescriptions")
public class PatientPrescriptionController {

    private final PatientPrescriptionService prescriptionService;

    public PatientPrescriptionController(PatientPrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> uploadPrescription(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "notes", required = false) String notes,
            Authentication authentication) {
        try {
            PrescriptionResponse response = prescriptionService.uploadPrescription(authentication.getName(), file, notes);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Prescription uploaded successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, e.getMessage(), "BAD_REQUEST"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Failed to upload file", "SERVER_ERROR"));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getMyPrescriptions(Authentication authentication) {
        List<PrescriptionResponse> list = prescriptionService.getMyPrescriptions(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Prescriptions retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> getPrescriptionDetails(@PathVariable Long id, Authentication authentication) {
        try {
            PrescriptionResponse response = prescriptionService.getPrescriptionDetails(authentication.getName(), id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Prescription details retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, e.getMessage(), "NOT_FOUND"));
        }
    }
}
