package com.medisync.dto;

import java.time.LocalTime;
import java.time.LocalDate;

public class ReminderResponse {
    private Long scheduleId;
    private Long patientMedicineId;
    private String medicineName;
    private String dosage;
    private String instructions;
    private LocalTime dueTime;
    private LocalDate dueDate;

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public Long getPatientMedicineId() { return patientMedicineId; }
    public void setPatientMedicineId(Long patientMedicineId) { this.patientMedicineId = patientMedicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public LocalTime getDueTime() { return dueTime; }
    public void setDueTime(LocalTime dueTime) { this.dueTime = dueTime; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
