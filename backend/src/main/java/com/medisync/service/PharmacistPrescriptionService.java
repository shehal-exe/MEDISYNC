package com.medisync.service;

import com.medisync.dao.PatientProfileDao;
import com.medisync.dao.PharmacistProfileDao;
import com.medisync.dao.PrescriptionDao;
import com.medisync.dao.UserDao;
import com.medisync.dto.PharmacistProfileResponse;
import com.medisync.dto.PrescriptionResponse;
import com.medisync.dto.UpdatePrescriptionStatusRequest;
import com.medisync.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PharmacistPrescriptionService {

    private final PrescriptionDao prescriptionDao;
    private final PharmacistProfileDao pharmacistProfileDao;
    private final PatientProfileDao patientProfileDao;
    private final NotificationService notificationService;
    private final UserDao userDao;

    public PharmacistPrescriptionService(PrescriptionDao prescriptionDao, 
                                         PharmacistProfileDao pharmacistProfileDao, 
                                         PatientProfileDao patientProfileDao, 
                                         NotificationService notificationService, 
                                         UserDao userDao) {
        this.prescriptionDao = prescriptionDao;
        this.pharmacistProfileDao = pharmacistProfileDao;
        this.patientProfileDao = patientProfileDao;
        this.notificationService = notificationService;
        this.userDao = userDao;
    }

    private Long getPharmacistId(String email) {
        User user = userDao.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("User not found");
        PharmacistProfileResponse profile = pharmacistProfileDao.getProfileByUserId(user.getUserId());
        if (profile == null) throw new IllegalArgumentException("Pharmacist profile not found");
        return profile.getPharmacistId();
    }

    public List<PrescriptionResponse> getAllPrescriptions(String status) {
        if (status != null && !status.trim().isEmpty()) {
            return prescriptionDao.findByStatus(status.toUpperCase());
        }
        return prescriptionDao.findAll();
    }

    public PrescriptionResponse getPrescription(Long id) {
        PrescriptionResponse res = prescriptionDao.findById(id);
        if (res == null) throw new IllegalArgumentException("Prescription not found");
        return res;
    }

    public PrescriptionResponse updateStatus(String email, Long prescriptionId, UpdatePrescriptionStatusRequest request) {
        Long pharmacistId = getPharmacistId(email);
        
        PrescriptionResponse existing = prescriptionDao.findById(prescriptionId);
        if (existing == null) {
            throw new IllegalArgumentException("Prescription not found");
        }
        
        int rows = prescriptionDao.updateStatus(prescriptionId, pharmacistId, request.getStatus(), request.getNotes());
        if (rows == 0) {
            throw new IllegalArgumentException("Failed to update prescription");
        }
        
        // Push Notification to the Patient
        Long patientUserId = patientProfileDao.getUserIdByPatientId(existing.getPatientId());
        if (patientUserId != null) {
            String msg = "Your prescription (ID: " + prescriptionId + ") has been " + request.getStatus();
            notificationService.createNotification(patientUserId, msg);
        }

        return prescriptionDao.findById(prescriptionId);
    }
}
