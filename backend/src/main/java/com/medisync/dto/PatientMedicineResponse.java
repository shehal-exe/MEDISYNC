package com.medisync.dto;

import java.time.LocalDateTime;

public class PatientMedicineResponse {
    private Long patientMedicineId;
    private Long patientId;
    private Long medicineId;
    private String medicineName;
    private String dosage;
    private String instructions;
    private Boolean isActive;
    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getPatientMedicineId() { return patientMedicineId; }
    public void setPatientMedicineId(Long patientMedicineId) { this.patientMedicineId = patientMedicineId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
