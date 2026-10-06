package com.medisync.controller;

import com.medisync.dto.ApiResponse;
import com.medisync.dto.RefillRequestResponse;
import com.medisync.dto.UpdateRefillStatusRequest;
import com.medisync.service.RefillRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pharmacist/refills")
public class PharmacistRefillController {

    private final RefillRequestService refillRequestService;

    public PharmacistRefillController(RefillRequestService refillRequestService) {
        this.refillRequestService = refillRequestService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RefillRequestResponse>>> getRequests(
            @RequestParam(required = false) String status,
            Authentication authentication) {
        List<RefillRequestResponse> requests = refillRequestService.getPharmacistRequests(authentication.getName(), status);
        return ResponseEntity.ok(new ApiResponse<>(true, "Refill requests retrieved successfully", requests));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RefillRequestResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRefillStatusRequest request,
            Authentication authentication) {
        try {
            RefillRequestResponse response = refillRequestService.updateStatus(authentication.getName(), id, request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Refill request updated successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), "BAD_REQUEST"));
        }
    }
}
