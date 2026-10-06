package com.medisync.service;

import com.medisync.dao.PatientProfileDao;
import com.medisync.dao.PrescriptionDao;
import com.medisync.dao.PrescriptionItemDao;
import com.medisync.dao.UserDao;
import com.medisync.dto.PatientProfileResponse;
import com.medisync.dto.PrescriptionResponse;
import com.medisync.model.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class PatientPrescriptionService {

    private final PrescriptionDao prescriptionDao;
    private final PrescriptionItemDao prescriptionItemDao;
    private final PatientProfileDao patientProfileDao;
    private final UserDao userDao;
    
    private final String UPLOAD_DIR = "uploads/prescriptions/";

    public PatientPrescriptionService(PrescriptionDao prescriptionDao, 
                                      PrescriptionItemDao prescriptionItemDao,
                                      PatientProfileDao patientProfileDao, 
                                      UserDao userDao) {
        this.prescriptionDao = prescriptionDao;
        this.prescriptionItemDao = prescriptionItemDao;
        this.patientProfileDao = patientProfileDao;
        this.userDao = userDao;
        
        // Create directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    private Long getPatientIdByEmail(String email) {
        User user = userDao.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("User not found");
        PatientProfileResponse profile = patientProfileDao.getProfileByUserId(user.getUserId());
        if (profile == null) throw new IllegalArgumentException("Patient profile not found");
        return profile.getPatientId();
    }

    public PrescriptionResponse uploadPrescription(String email, MultipartFile file, String notes) {
        Long patientId = getPatientIdByEmail(email);
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty file.");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            
            Path targetLocation = Paths.get(UPLOAD_DIR).resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            String filePath = targetLocation.toString();
            Long prescriptionId = prescriptionDao.create(patientId, filePath, notes);
            
            return prescriptionDao.findByIdAndPatientId(prescriptionId, patientId);
            
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file", ex);
        }
    }

    public List<PrescriptionResponse> getMyPrescriptions(String email) {
        Long patientId = getPatientIdByEmail(email);
        return prescriptionDao.findAllByPatientId(patientId);
    }

    public PrescriptionResponse getPrescriptionDetails(String email, Long prescriptionId) {
        Long patientId = getPatientIdByEmail(email);
        PrescriptionResponse prescription = prescriptionDao.findByIdAndPatientId(prescriptionId, patientId);
        
        if (prescription == null) {
            throw new IllegalArgumentException("Prescription not found or access denied");
        }
        
        // If verified, fetch items
        if ("VERIFIED".equals(prescription.getStatus())) {
            prescription.setItems(prescriptionItemDao.findAllByPrescriptionId(prescriptionId));
        }
        
        return prescription;
    }
}
