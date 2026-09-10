package com.medisync.service;

import com.medisync.dao.MedicineDao;
import com.medisync.dao.PatientMedicineDao;
import com.medisync.dao.PatientProfileDao;
import com.medisync.dao.UserDao;
import com.medisync.dto.CreatePatientMedicineRequest;
import com.medisync.dto.PatientMedicineResponse;
import com.medisync.dto.PatientProfileResponse;
import com.medisync.dto.UpdatePatientMedicineRequest;
import com.medisync.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientMedicineService {

    private final PatientMedicineDao patientMedicineDao;
    private final MedicineDao medicineDao;
    private final PatientProfileDao patientProfileDao;
    private final UserDao userDao;

    public PatientMedicineService(PatientMedicineDao patientMedicineDao, MedicineDao medicineDao, 
                                  PatientProfileDao patientProfileDao, UserDao userDao) {
        this.patientMedicineDao = patientMedicineDao;
        this.medicineDao = medicineDao;
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

    public List<PatientMedicineResponse> getMyMedicines(String email) {
        Long patientId = getPatientIdByEmail(email);
        return patientMedicineDao.findAllByPatientId(patientId);
    }

    public PatientMedicineResponse getMyMedicineById(String email, Long patientMedicineId) {
        Long patientId = getPatientIdByEmail(email);
        PatientMedicineResponse response = patientMedicineDao.findByIdAndPatientId(patientMedicineId, patientId);
        if (response == null) {
            throw new SecurityException("Medicine not found or access denied");
        }
        return response;
    }

    public PatientMedicineResponse addMedicine(String email, CreatePatientMedicineRequest request) {
        Long patientId = getPatientIdByEmail(email);
        if (!medicineDao.existsById(request.getMedicineId())) {
            throw new IllegalArgumentException("Invalid medicine ID");
        }
        Long id = patientMedicineDao.create(patientId, request.getMedicineId(), request.getDosage(), request.getInstructions());
        return patientMedicineDao.findByIdAndPatientId(id, patientId);
    }

    public PatientMedicineResponse updateMedicine(String email, Long patientMedicineId, UpdatePatientMedicineRequest request) {
        Long patientId = getPatientIdByEmail(email);
        int rows = patientMedicineDao.update(patientMedicineId, patientId, request.getDosage(), request.getInstructions(), request.getIsActive());
        if (rows == 0) {
            throw new SecurityException("Medicine not found or access denied");
        }
        return patientMedicineDao.findByIdAndPatientId(patientMedicineId, patientId);
    }

    public void deleteMedicine(String email, Long patientMedicineId) {
        Long patientId = getPatientIdByEmail(email);
        int rows = patientMedicineDao.delete(patientMedicineId, patientId);
        if (rows == 0) {
            throw new SecurityException("Medicine not found or access denied");
        }
    }
}
