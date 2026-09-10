package com.medisync.dto;

import jakarta.validation.constraints.Size;

public class UpdatePatientMedicineRequest {

    @Size(max = 100, message = "Dosage must be under 100 characters")
    private String dosage;

    private String instructions;
    
    private Boolean isActive;

    // Getters and Setters
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
