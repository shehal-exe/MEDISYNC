package com.medisync.service;

import com.medisync.dao.MedicationLogDao;
import com.medisync.dao.MedicationScheduleDao;
import com.medisync.dao.PatientProfileDao;
import com.medisync.dao.UserDao;
import com.medisync.dto.AdherenceResponse;
import com.medisync.dto.MedicationLogResponse;
import com.medisync.dto.MedicationScheduleResponse;
import com.medisync.dto.PatientProfileResponse;
import com.medisync.dto.ReminderResponse;
import com.medisync.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MedicationReminderService {

    private final MedicationLogDao medicationLogDao;
    private final MedicationScheduleDao medicationScheduleDao;
    private final PatientProfileDao patientProfileDao;
    private final UserDao userDao;

    public MedicationReminderService(MedicationLogDao medicationLogDao, 
                                     MedicationScheduleDao medicationScheduleDao,
                                     PatientProfileDao patientProfileDao, 
                                     UserDao userDao) {
        this.medicationLogDao = medicationLogDao;
        this.medicationScheduleDao = medicationScheduleDao;
        this.patientProfileDao = patientProfileDao;
        this.userDao = userDao;
    }

    private Long getPatientIdByEmail(String email) {
        User user = userDao.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("User not found");
        PatientProfileResponse profile = patientProfileDao.getProfileByUserId(user.getUserId());
        if (profile == null) throw new IllegalArgumentException("Patient profile not found");
        return profile.getPatientId();
    }

    public List<ReminderResponse> getUpcomingReminders(String email) {
        Long patientId = getPatientIdByEmail(email);
        List<MedicationScheduleResponse> schedules = medicationScheduleDao.findAllByPatientId(patientId);
        List<ReminderResponse> reminders = new ArrayList<>();
        
        LocalDate today = LocalDate.now();

        for (MedicationScheduleResponse schedule : schedules) {
            // Very simple logic: if start date is in the future or end date passed, skip
            if (schedule.getStartDate() != null && schedule.getStartDate().isAfter(today)) continue;
            if (schedule.getEndDate() != null && schedule.getEndDate().isBefore(today)) continue;

            // Check if there is already a log for today (meaning they took it, skipped it, etc.)
            boolean handledToday = medicationLogDao.hasLogForScheduleAndDate(schedule.getScheduleId(), today);
            
            if (!handledToday) {
                ReminderResponse reminder = new ReminderResponse();
                reminder.setScheduleId(schedule.getScheduleId());
                reminder.setPatientMedicineId(schedule.getPatientMedicineId());
                reminder.setMedicineName(schedule.getMedicineName());
                reminder.setDueTime(schedule.getTimeOfDay());
                reminder.setDueDate(today);
                // Dosage and Instructions would need joining or can be ignored if frontend has them
                reminders.add(reminder);
            }
        }
        
        return reminders;
    }

    public void markReminder(String email, Long scheduleId, String status) {
        Long patientId = getPatientIdByEmail(email);
        
        // Verify ownership
        MedicationScheduleResponse schedule = medicationScheduleDao.findByIdAndPatientId(scheduleId, patientId);
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule not found or access denied");
        }
        
        if (!status.equals("TAKEN") && !status.equals("SKIPPED") && !status.equals("MISSED")) {
            throw new IllegalArgumentException("Invalid status. Must be TAKEN, SKIPPED, or MISSED");
        }

        medicationLogDao.createLog(scheduleId, status, LocalDateTime.now());
    }

    public List<MedicationLogResponse> getHistory(String email) {
        Long patientId = getPatientIdByEmail(email);
        return medicationLogDao.findLogsByPatientId(patientId);
    }

    public AdherenceResponse getAdherence(String email) {
        Long patientId = getPatientIdByEmail(email);
        List<MedicationLogResponse> logs = medicationLogDao.findLogsByPatientId(patientId);
        
        int taken = 0;
        int missed = 0;
        int skipped = 0;
        
        for (MedicationLogResponse log : logs) {
            if ("TAKEN".equals(log.getStatus())) taken++;
            else if ("MISSED".equals(log.getStatus())) missed++;
            else if ("SKIPPED".equals(log.getStatus())) skipped++;
        }
        
        int total = taken + missed + skipped;
        double percentage = total == 0 ? 0.0 : ((double) taken / total) * 100.0;
        
        AdherenceResponse response = new AdherenceResponse();
        response.setTotalTaken(taken);
        response.setTotalMissed(missed);
        response.setTotalSkipped(skipped);
        response.setAdherencePercentage(percentage);
        return response;
    }
}
