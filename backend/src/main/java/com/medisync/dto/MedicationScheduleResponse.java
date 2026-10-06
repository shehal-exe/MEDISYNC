package com.medisync.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class MedicationScheduleResponse {
    private Long scheduleId;
    private Long patientMedicineId;
    private String medicineName; // Join from Medicine via PatientMedicine
    private String frequency;
    private LocalTime timeOfDay;
    private LocalDate startDate;
    private LocalDate endDate;

    // Getters and Setters
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public Long getPatientMedicineId() { return patientMedicineId; }
    public void setPatientMedicineId(Long patientMedicineId) { this.patientMedicineId = patientMedicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public LocalTime getTimeOfDay() { return timeOfDay; }
    public void setTimeOfDay(LocalTime timeOfDay) { this.timeOfDay = timeOfDay; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
