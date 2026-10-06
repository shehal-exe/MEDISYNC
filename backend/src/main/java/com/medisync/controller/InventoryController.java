package com.medisync.controller;

import com.medisync.dto.ApiResponse;
import com.medisync.dto.AddStockRequest;
import com.medisync.dto.MedicineRequest;
import com.medisync.dto.MedicineResponse;
import com.medisync.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pharmacist/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getAll(Authentication authentication) {
        List<MedicineResponse> list = inventoryService.getAllMedicines(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Medicines retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicineResponse>> getOne(@PathVariable Long id, Authentication authentication) {
        try {
            MedicineResponse res = inventoryService.getMedicine(authentication.getName(), id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Medicine retrieved successfully", res));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, e.getMessage(), "NOT_FOUND"));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MedicineResponse>> add(@Valid @RequestBody MedicineRequest request, Authentication authentication) {
        MedicineResponse res = inventoryService.addMedicine(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Medicine added successfully", res));
    }

    @PostMapping("/stock")
    public ResponseEntity<ApiResponse<MedicineResponse>> addStock(@Valid @RequestBody AddStockRequest request, Authentication authentication) {
        try {
            MedicineResponse res = inventoryService.addStock(authentication.getName(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Stock batch added successfully", res));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, e.getMessage(), "BAD_REQUEST"));
        } catch (org.springframework.dao.DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse<>(false, "Batch number already exists for this medicine", "DUPLICATE_BATCH"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicineResponse>> update(@PathVariable Long id, @Valid @RequestBody MedicineRequest request, Authentication authentication) {
        try {
            MedicineResponse res = inventoryService.updateMedicine(authentication.getName(), id, request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Medicine updated successfully", res));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, e.getMessage(), "NOT_FOUND"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, Authentication authentication) {
        try {
            inventoryService.deleteMedicine(authentication.getName(), id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Medicine deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, e.getMessage(), "NOT_FOUND"));
        }
    }
}
