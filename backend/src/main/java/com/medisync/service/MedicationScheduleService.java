package com.medisync.service;

import com.medisync.dao.MedicationScheduleDao;
import com.medisync.dao.PatientMedicineDao;
import com.medisync.dao.PatientProfileDao;
import com.medisync.dto.CreateMedicationScheduleRequest;
import com.medisync.dto.MedicationScheduleResponse;
import com.medisync.dto.PatientMedicineResponse;
import com.medisync.dto.UpdateMedicationScheduleRequest;
import com.medisync.dto.UpdateMedicationScheduleRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicationScheduleService {

    private final MedicationScheduleDao medicationScheduleDao;
    private final PatientProfileDao patientProfileDao;
    private final PatientMedicineDao patientMedicineDao;
    private final com.medisync.dao.UserDao userDao;

    public MedicationScheduleService(MedicationScheduleDao medicationScheduleDao,
                                     PatientProfileDao patientProfileDao,
                                     PatientMedicineDao patientMedicineDao,
                                     com.medisync.dao.UserDao userDao) {
        this.medicationScheduleDao = medicationScheduleDao;
        this.patientProfileDao = patientProfileDao;
        this.patientMedicineDao = patientMedicineDao;
        this.userDao = userDao;
    }

    private Long getPatientIdForUser(String email) {
        com.medisync.model.User user = userDao.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("User not found");
        com.medisync.dto.PatientProfileResponse profile = patientProfileDao.getProfileByUserId(user.getUserId());
        if (profile == null) throw new IllegalArgumentException("Patient profile not found");
        return profile.getPatientId();
    }

    public List<MedicationScheduleResponse> getSchedules(String email) {
        Long patientId = getPatientIdForUser(email);
        return medicationScheduleDao.findAllByPatientId(patientId);
    }

    public MedicationScheduleResponse getScheduleById(String email, Long scheduleId) {
        Long patientId = getPatientIdForUser(email);
        MedicationScheduleResponse response = medicationScheduleDao.findByIdAndPatientId(scheduleId, patientId);
        if (response == null) {
            throw new IllegalArgumentException("Medication schedule not found or unauthorized");
        }
        return response;
    }

    public MedicationScheduleResponse createSchedule(String email, CreateMedicationScheduleRequest request) {
        Long patientId = getPatientIdForUser(email);
        
        // Verify that the requested patientMedicineId belongs to this patient
        PatientMedicineResponse pm = patientMedicineDao.findByIdAndPatientId(request.getPatientMedicineId(), patientId);
        if (pm == null) {
            throw new IllegalArgumentException("Patient medicine not found or unauthorized");
        }

        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        Long scheduleId = medicationScheduleDao.create(
                request.getPatientMedicineId(),
                request.getFrequency(),
                request.getTimeOfDay(),
                request.getStartDate(),
                request.getEndDate()
        );

        return medicationScheduleDao.findByIdAndPatientId(scheduleId, patientId);
    }

    public MedicationScheduleResponse updateSchedule(String email, Long scheduleId, UpdateMedicationScheduleRequest request) {
        Long patientId = getPatientIdForUser(email);
        
        // Verify ownership
        MedicationScheduleResponse existing = medicationScheduleDao.findByIdAndPatientId(scheduleId, patientId);
        if (existing == null) {
            throw new IllegalArgumentException("Medication schedule not found or unauthorized");
        }

        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        medicationScheduleDao.update(
                scheduleId,
                request.getFrequency(),
                request.getTimeOfDay(),
                request.getStartDate(),
                request.getEndDate()
        );

        return medicationScheduleDao.findByIdAndPatientId(scheduleId, patientId);
    }

    public void deleteSchedule(String email, Long scheduleId) {
        Long patientId = getPatientIdForUser(email);
        
        // Verify ownership
        MedicationScheduleResponse existing = medicationScheduleDao.findByIdAndPatientId(scheduleId, patientId);
        if (existing == null) {
            throw new IllegalArgumentException("Medication schedule not found or unauthorized");
        }

        medicationScheduleDao.delete(scheduleId);
    }
}
