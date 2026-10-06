package com.medisync.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateRefillStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(APPROVED|COMPLETED|REJECTED)$", message = "Status must be APPROVED, COMPLETED, or REJECTED")
    private String status;

    private String notes;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
