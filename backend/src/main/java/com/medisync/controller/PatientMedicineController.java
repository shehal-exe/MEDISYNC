package com.medisync.controller;

import com.medisync.dto.ApiResponse;
import com.medisync.dto.CreatePatientMedicineRequest;
import com.medisync.dto.PatientMedicineResponse;
import com.medisync.dto.UpdatePatientMedicineRequest;
import com.medisync.service.PatientMedicineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patient/medicines")
public class PatientMedicineController {

    private final PatientMedicineService patientMedicineService;

    public PatientMedicineController(PatientMedicineService patientMedicineService) {
        this.patientMedicineService = patientMedicineService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientMedicineResponse>>> getMyMedicines(Authentication authentication) {
        List<PatientMedicineResponse> medicines = patientMedicineService.getMyMedicines(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Medicines fetched successfully", medicines));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientMedicineResponse>> getMyMedicineById(@PathVariable Long id, Authentication authentication) {
        try {
            PatientMedicineResponse medicine = patientMedicineService.getMyMedicineById(authentication.getName(), id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Medicine fetched successfully", medicine));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, e.getMessage(), "ACCESS_DENIED"));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PatientMedicineResponse>> addMedicine(
            @Valid @RequestBody CreatePatientMedicineRequest request, Authentication authentication) {
        try {
            PatientMedicineResponse medicine = patientMedicineService.addMedicine(authentication.getName(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Medicine added successfully", medicine));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, e.getMessage(), "INVALID_REQUEST"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientMedicineResponse>> updateMedicine(
            @PathVariable Long id, @Valid @RequestBody UpdatePatientMedicineRequest request, Authentication authentication) {
        try {
            PatientMedicineResponse medicine = patientMedicineService.updateMedicine(authentication.getName(), id, request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Medicine updated successfully", medicine));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, e.getMessage(), "ACCESS_DENIED"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMedicine(@PathVariable Long id, Authentication authentication) {
        try {
            patientMedicineService.deleteMedicine(authentication.getName(), id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Medicine deleted successfully"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, e.getMessage(), "ACCESS_DENIED"));
        }
    }
}
