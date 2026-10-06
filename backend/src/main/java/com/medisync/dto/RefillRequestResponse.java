package com.medisync.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RefillRequestResponse {
    private Long refillRequestId;
    private Long patientId;
    private String patientName;
    private String patientEmail;
    private String medicineName;
    private Integer quantity;
    private String fulfillmentMethod;
    private BigDecimal estimatedTotal;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime updatedAt;
    private String pharmacistNotes;
    private Long approvedBy;
    private Long requestedPharmacistId;
    private String requestedPharmacistName;
    private String requestedPharmacistHandle;

    public Long getRefillRequestId() { return refillRequestId; }
    public void setRefillRequestId(Long refillRequestId) { this.refillRequestId = refillRequestId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientEmail() { return patientEmail; }
    public void setPatientEmail(String patientEmail) { this.patientEmail = patientEmail; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getFulfillmentMethod() { return fulfillmentMethod; }
    public void setFulfillmentMethod(String fulfillmentMethod) { this.fulfillmentMethod = fulfillmentMethod; }

    public BigDecimal getEstimatedTotal() { return estimatedTotal; }
    public void setEstimatedTotal(BigDecimal estimatedTotal) { this.estimatedTotal = estimatedTotal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getPharmacistNotes() { return pharmacistNotes; }
    public void setPharmacistNotes(String pharmacistNotes) { this.pharmacistNotes = pharmacistNotes; }

    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }

    public Long getRequestedPharmacistId() { return requestedPharmacistId; }
    public void setRequestedPharmacistId(Long requestedPharmacistId) { this.requestedPharmacistId = requestedPharmacistId; }

    public String getRequestedPharmacistName() { return requestedPharmacistName; }
    public void setRequestedPharmacistName(String requestedPharmacistName) { this.requestedPharmacistName = requestedPharmacistName; }

    public String getRequestedPharmacistHandle() { return requestedPharmacistHandle; }
    public void setRequestedPharmacistHandle(String requestedPharmacistHandle) { this.requestedPharmacistHandle = requestedPharmacistHandle; }
}
