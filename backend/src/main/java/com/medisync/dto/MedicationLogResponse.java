package com.medisync.dto;

import java.time.LocalDateTime;

public class MedicationLogResponse {
    private Long logId;
    private Long scheduleId;
    private String medicineName;
    private LocalDateTime logTime;
    private String status;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public LocalDateTime getLogTime() { return logTime; }
    public void setLogTime(LocalDateTime logTime) { this.logTime = logTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
