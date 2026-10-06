package com.medisync.dto;

public class AdherenceResponse {
    private int totalTaken;
    private int totalMissed;
    private int totalSkipped;
    private double adherencePercentage;

    public int getTotalTaken() { return totalTaken; }
    public void setTotalTaken(int totalTaken) { this.totalTaken = totalTaken; }

    public int getTotalMissed() { return totalMissed; }
    public void setTotalMissed(int totalMissed) { this.totalMissed = totalMissed; }

    public int getTotalSkipped() { return totalSkipped; }
    public void setTotalSkipped(int totalSkipped) { this.totalSkipped = totalSkipped; }

    public double getAdherencePercentage() { return adherencePercentage; }
    public void setAdherencePercentage(double adherencePercentage) { this.adherencePercentage = adherencePercentage; }
}
