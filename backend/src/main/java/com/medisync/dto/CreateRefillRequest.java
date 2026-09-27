package com.medisync.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CreateRefillRequest {

    @NotBlank(message = "Medicine name is required")
    @Size(max = 255, message = "Medicine name must not exceed 255 characters")
    private String medicineName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 50, message = "Quantity cannot exceed 50")
    private Integer quantity;

    @NotBlank(message = "Fulfillment method is required")
    @Pattern(regexp = "^(Pickup|Delivery)$", message = "Fulfillment method must be Pickup or Delivery")
    private String fulfillmentMethod;

    @NotNull(message = "Estimated total is required")
    @DecimalMin(value = "0.01", message = "Estimated total must be greater than zero")
    private BigDecimal estimatedTotal;

    @NotNull(message = "Please choose the pharmacist or pharmacy for this refill")
    private Long pharmacistId;

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getFulfillmentMethod() { return fulfillmentMethod; }
    public void setFulfillmentMethod(String fulfillmentMethod) { this.fulfillmentMethod = fulfillmentMethod; }

    public BigDecimal getEstimatedTotal() { return estimatedTotal; }
    public void setEstimatedTotal(BigDecimal estimatedTotal) { this.estimatedTotal = estimatedTotal; }

    public Long getPharmacistId() { return pharmacistId; }
    public void setPharmacistId(Long pharmacistId) { this.pharmacistId = pharmacistId; }
}
