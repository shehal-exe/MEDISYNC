package com.medisync.controller;

import com.medisync.dto.ApiResponse;
import com.medisync.dto.MedicineRequest;
import com.medisync.dto.MedicineResponse;
import com.medisync.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getAll() {
        List<MedicineResponse> list = inventoryService.getAllMedicines();
        return ResponseEntity.ok(new ApiResponse<>(true, "Medicines retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicineResponse>> getOne(@PathVariable Long id) {
        try {
            MedicineResponse res = inventoryService.getMedicine(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Medicine retrieved successfully", res));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, e.getMessage(), "NOT_FOUND"));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MedicineResponse>> add(@Valid @RequestBody MedicineRequest request) {
        MedicineResponse res = inventoryService.addMedicine(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Medicine added successfully", res));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicineResponse>> update(@PathVariable Long id, @Valid @RequestBody MedicineRequest request) {
        try {
            MedicineResponse res = inventoryService.updateMedicine(id, request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Medicine updated successfully", res));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, e.getMessage(), "NOT_FOUND"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            inventoryService.deleteMedicine(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Medicine deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, e.getMessage(), "NOT_FOUND"));
        }
    }
}
