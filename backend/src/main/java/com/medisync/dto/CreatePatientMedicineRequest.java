package com.medisync.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreatePatientMedicineRequest {

    @NotNull(message = "Medicine ID is required")
    private Long medicineId;

    @Size(max = 100, message = "Dosage must be under 100 characters")
    private String dosage;

    private String instructions;

    // Getters and Setters
    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}
