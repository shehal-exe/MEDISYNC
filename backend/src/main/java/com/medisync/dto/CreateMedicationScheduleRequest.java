package com.medisync.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class CreateMedicationScheduleRequest {

    @NotNull(message = "Patient Medicine ID is required")
    private Long patientMedicineId;

    @NotBlank(message = "Frequency is required")
    private String frequency;

    @NotNull(message = "Time of day is required")
    private LocalTime timeOfDay;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    public Long getPatientMedicineId() { return patientMedicineId; }
    public void setPatientMedicineId(Long patientMedicineId) { this.patientMedicineId = patientMedicineId; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public LocalTime getTimeOfDay() { return timeOfDay; }
    public void setTimeOfDay(LocalTime timeOfDay) { this.timeOfDay = timeOfDay; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
