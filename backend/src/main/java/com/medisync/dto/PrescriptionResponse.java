package com.medisync.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PrescriptionResponse {
    private Long prescriptionId;
    private LocalDateTime uploadDate;
    private String filePath;
    private String status;
    private String notes;
    private List<PrescriptionItemResponse> items;

    public Long getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<PrescriptionItemResponse> getItems() { return items; }
    public void setItems(List<PrescriptionItemResponse> items) { this.items = items; }
}
