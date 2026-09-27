package com.medisync.controller;

import com.medisync.dto.ApiResponse;
import com.medisync.dto.CreateRefillRequest;
import com.medisync.dto.PharmacistProfileResponse;
import com.medisync.dto.RefillRequestResponse;
import com.medisync.service.RefillRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patient/refills")
public class PatientRefillController {

    private final RefillRequestService refillRequestService;

    public PatientRefillController(RefillRequestService refillRequestService) {
        this.refillRequestService = refillRequestService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RefillRequestResponse>>> getMyRequests(Authentication authentication) {
        List<RefillRequestResponse> requests = refillRequestService.getMyRequests(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Refill requests retrieved successfully", requests));
    }

    @GetMapping("/pharmacists")
    public ResponseEntity<ApiResponse<List<PharmacistProfileResponse>>> getAvailablePharmacists() {
        List<PharmacistProfileResponse> pharmacists = refillRequestService.getAvailablePharmacists();
        return ResponseEntity.ok(new ApiResponse<>(true, "Available pharmacists retrieved successfully", pharmacists));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RefillRequestResponse>> createRequest(
            @Valid @RequestBody CreateRefillRequest request,
            Authentication authentication) {
        try {
            RefillRequestResponse response = refillRequestService.createPatientRequest(authentication.getName(), request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Refill request submitted successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), "BAD_REQUEST"));
        }
    }
}
