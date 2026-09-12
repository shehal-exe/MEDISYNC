package com.medisync.controller;

import com.medisync.dto.ApiResponse;
import com.medisync.dto.CreateSaleRequest;
import com.medisync.dto.SaleResponse;
import com.medisync.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pharmacist/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SaleResponse>> processSale(
            @Valid @RequestBody CreateSaleRequest request,
            Authentication authentication) {
        try {
            SaleResponse response = saleService.createSale(authentication.getName(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Sale processed successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, e.getMessage(), "BAD_REQUEST"));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SaleResponse>>> getAllSales() {
        List<SaleResponse> list = saleService.getAllSales();
        return ResponseEntity.ok(new ApiResponse<>(true, "Sales retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SaleResponse>> getSaleDetails(@PathVariable Long id) {
        try {
            SaleResponse response = saleService.getSaleById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Sale details retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, e.getMessage(), "NOT_FOUND"));
        }
    }
}
