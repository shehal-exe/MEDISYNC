package com.medisync.controller;

import com.medisync.dto.ApiResponse;
import com.medisync.dto.PharmacistProfileResponse;
import com.medisync.dto.UpdatePharmacistProfileRequest;
import com.medisync.service.PharmacistProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pharmacist/profile")
public class PharmacistProfileController {

    private final PharmacistProfileService pharmacistProfileService;

    public PharmacistProfileController(PharmacistProfileService pharmacistProfileService) {
        this.pharmacistProfileService = pharmacistProfileService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PharmacistProfileResponse>> getMyProfile(Authentication authentication) {
        try {
            PharmacistProfileResponse response = pharmacistProfileService.getMyProfile(authentication.getName());
            return ResponseEntity.ok(new ApiResponse<>(true, "Profile retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, e.getMessage(), "NOT_FOUND"));
        }
    }

    @PutMapping
    public ResponseEntity<ApiResponse<PharmacistProfileResponse>> updateMyProfile(
            @Valid @RequestBody UpdatePharmacistProfileRequest request,
            Authentication authentication) {
        try {
            PharmacistProfileResponse response = pharmacistProfileService.updateMyProfile(authentication.getName(), request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, e.getMessage(), "BAD_REQUEST"));
        }
    }
}
