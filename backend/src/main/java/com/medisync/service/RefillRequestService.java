package com.medisync.service;

import com.medisync.dao.PatientProfileDao;
import com.medisync.dao.PharmacistProfileDao;
import com.medisync.dao.RefillRequestDao;
import com.medisync.dao.UserDao;
import com.medisync.dto.CreateRefillRequest;
import com.medisync.dto.PatientProfileResponse;
import com.medisync.dto.PharmacistProfileResponse;
import com.medisync.dto.RefillRequestResponse;
import com.medisync.dto.UpdateRefillStatusRequest;
import com.medisync.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RefillRequestService {

    private final RefillRequestDao refillRequestDao;
    private final PatientProfileDao patientProfileDao;
    private final PharmacistProfileDao pharmacistProfileDao;
    private final NotificationService notificationService;
    private final UserDao userDao;

    public RefillRequestService(RefillRequestDao refillRequestDao,
                                PatientProfileDao patientProfileDao,
                                PharmacistProfileDao pharmacistProfileDao,
                                NotificationService notificationService,
                                UserDao userDao) {
        this.refillRequestDao = refillRequestDao;
        this.patientProfileDao = patientProfileDao;
        this.pharmacistProfileDao = pharmacistProfileDao;
        this.notificationService = notificationService;
        this.userDao = userDao;
    }

    public RefillRequestResponse createPatientRequest(String email, CreateRefillRequest request) {
        PatientProfileResponse patient = getPatientProfile(email);
        PharmacistProfileResponse targetPharmacist = pharmacistProfileDao.getProfileByPharmacistId(request.getPharmacistId());
        if (targetPharmacist == null) {
            throw new IllegalArgumentException("Selected pharmacist was not found. Please choose a valid pharmacy destination.");
        }
        Long requestId = refillRequestDao.create(patient.getPatientId(), request);

        String patientName = patient.getFirstName() + " " + patient.getLastName();
        String message = "New refill request from " + patientName + " for " +
                request.getQuantity() + " x " + request.getMedicineName() + ".";
        Long pharmacistUserId = pharmacistProfileDao.getUserIdByPharmacistId(targetPharmacist.getPharmacistId());
        if (pharmacistUserId != null) {
            notificationService.createNotification(pharmacistUserId, message);
        }

        return refillRequestDao.findById(requestId);
    }

    public List<RefillRequestResponse> getMyRequests(String email) {
        PatientProfileResponse patient = getPatientProfile(email);
        return refillRequestDao.findAllByPatientId(patient.getPatientId());
    }

    public List<PharmacistProfileResponse> getAvailablePharmacists() {
        return pharmacistProfileDao.findAllProfiles();
    }

    public List<RefillRequestResponse> getPharmacistRequests(String email, String status) {
        PharmacistProfileResponse pharmacist = getPharmacistProfile(email);
        if (status != null && !status.trim().isEmpty()) {
            return refillRequestDao.findByRequestedPharmacistIdAndStatus(
                    pharmacist.getPharmacistId(),
                    status.trim().toUpperCase()
            );
        }
        return refillRequestDao.findAllByRequestedPharmacistId(pharmacist.getPharmacistId());
    }

    public RefillRequestResponse updateStatus(String email, Long refillRequestId, UpdateRefillStatusRequest request) {
        PharmacistProfileResponse pharmacist = getPharmacistProfile(email);
        RefillRequestResponse existing = refillRequestDao.findById(refillRequestId);
        if (existing == null) {
            throw new IllegalArgumentException("Refill request not found");
        }
        if (existing.getRequestedPharmacistId() == null ||
                !existing.getRequestedPharmacistId().equals(pharmacist.getPharmacistId())) {
            throw new IllegalArgumentException("This refill request was sent to another pharmacist.");
        }

        String status = request.getStatus().toUpperCase();
        String notes = request.getNotes();
        if (notes == null || notes.trim().isEmpty()) {
            notes = defaultNotes(status);
        }

        int rows = refillRequestDao.updateStatus(refillRequestId, pharmacist.getPharmacistId(), status, notes);
        if (rows == 0) {
            throw new IllegalArgumentException("Failed to update refill request");
        }

        Long patientUserId = patientProfileDao.getUserIdByPatientId(existing.getPatientId());
        if (patientUserId != null) {
            notificationService.createNotification(
                    patientUserId,
                    "Your refill request for " + existing.getMedicineName() + " has been " + status + "."
            );
        }

        return refillRequestDao.findById(refillRequestId);
    }

    private PatientProfileResponse getPatientProfile(String email) {
        User user = userDao.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("User not found");
        PatientProfileResponse patient = patientProfileDao.getProfileByUserId(user.getUserId());
        if (patient == null) throw new IllegalArgumentException("Patient profile not found");
        return patient;
    }

    private PharmacistProfileResponse getPharmacistProfile(String email) {
        User user = userDao.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("User not found");
        PharmacistProfileResponse pharmacist = pharmacistProfileDao.getProfileByUserId(user.getUserId());
        if (pharmacist == null) throw new IllegalArgumentException("Pharmacist profile not found");
        return pharmacist;
    }

    private String defaultNotes(String status) {
        if ("APPROVED".equals(status)) {
            return "Approved by pharmacist. Preparing the refill order.";
        }
        if ("COMPLETED".equals(status)) {
            return "Refill completed by pharmacy team.";
        }
        return "Request reviewed by pharmacist.";
    }
}
