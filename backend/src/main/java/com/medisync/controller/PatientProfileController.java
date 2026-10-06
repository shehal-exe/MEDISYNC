package com.medisync.controller;

import com.medisync.dto.ApiResponse;
import com.medisync.dto.PatientProfileResponse;
import com.medisync.dto.UpdatePatientProfileRequest;
import com.medisync.service.PatientProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientProfileController {

    private final PatientProfileService patientProfileService;

    public PatientProfileController(PatientProfileService patientProfileService) {
        this.patientProfileService = patientProfileService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> getMyProfile(Authentication authentication) {
        PatientProfileResponse profile = patientProfileService.getMyProfile(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile fetched successfully", profile));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> updateMyProfile(
            @Valid @RequestBody UpdatePatientProfileRequest request, Authentication authentication) {
        PatientProfileResponse profile = patientProfileService.updateMyProfile(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated successfully", profile));
    }
}
