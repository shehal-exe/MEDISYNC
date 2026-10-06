package com.medisync.service;

import com.medisync.dao.PatientProfileDao;
import com.medisync.dao.UserDao;
import com.medisync.dto.PatientProfileResponse;
import com.medisync.dto.UpdatePatientProfileRequest;
import com.medisync.model.User;
import org.springframework.stereotype.Service;

@Service
public class PatientProfileService {

    private final PatientProfileDao patientProfileDao;
    private final UserDao userDao;

    public PatientProfileService(PatientProfileDao patientProfileDao, UserDao userDao) {
        this.patientProfileDao = patientProfileDao;
        this.userDao = userDao;
    }

    public PatientProfileResponse getMyProfile(String email) {
        User user = userDao.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return patientProfileDao.getProfileByUserId(user.getUserId());
    }

    public PatientProfileResponse updateMyProfile(String email, UpdatePatientProfileRequest request) {
        User user = userDao.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        patientProfileDao.updateProfile(user.getUserId(), request.getFirstName(), request.getLastName(), 
                request.getDateOfBirth(), request.getContactNumber());
        return patientProfileDao.getProfileByUserId(user.getUserId());
    }
}
