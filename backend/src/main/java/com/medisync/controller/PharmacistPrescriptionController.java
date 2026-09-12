package com.medisync.controller;

import com.medisync.dto.ApiResponse;
import com.medisync.dto.PrescriptionResponse;
import com.medisync.dto.UpdatePrescriptionStatusRequest;
import com.medisync.service.PharmacistPrescriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pharmacist/prescriptions")
public class PharmacistPrescriptionController {

    private final PharmacistPrescriptionService pharmacistPrescriptionService;

    public PharmacistPrescriptionController(PharmacistPrescriptionService pharmacistPrescriptionService) {
        this.pharmacistPrescriptionService = pharmacistPrescriptionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getAll(@RequestParam(required = false) String status) {
        List<PrescriptionResponse> list = pharmacistPrescriptionService.getAllPrescriptions(status);
        return ResponseEntity.ok(new ApiResponse<>(true, "Prescriptions retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> getOne(@PathVariable Long id) {
        try {
            PrescriptionResponse res = pharmacistPrescriptionService.getPrescription(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Prescription retrieved successfully", res));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, e.getMessage(), "NOT_FOUND"));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePrescriptionStatusRequest request,
            Authentication authentication) {
        try {
            PrescriptionResponse res = pharmacistPrescriptionService.updateStatus(authentication.getName(), id, request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Prescription status updated successfully", res));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, e.getMessage(), "BAD_REQUEST"));
        }
    }
}
