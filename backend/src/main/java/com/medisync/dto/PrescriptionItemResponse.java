package com.medisync.dto;

public class PrescriptionItemResponse {
    private Long prescriptionItemId;
    private Long medicineId;
    private String medicineName;
    private Integer prescribedQuantity;
    private String dosageInstructions;

    public Long getPrescriptionItemId() { return prescriptionItemId; }
    public void setPrescriptionItemId(Long prescriptionItemId) { this.prescriptionItemId = prescriptionItemId; }

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public Integer getPrescribedQuantity() { return prescribedQuantity; }
    public void setPrescribedQuantity(Integer prescribedQuantity) { this.prescribedQuantity = prescribedQuantity; }

    public String getDosageInstructions() { return dosageInstructions; }
    public void setDosageInstructions(String dosageInstructions) { this.dosageInstructions = dosageInstructions; }
}
