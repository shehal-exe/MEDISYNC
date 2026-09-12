package com.medisync.service;

import com.medisync.dao.PharmacistProfileDao;
import com.medisync.dao.UserDao;
import com.medisync.dto.PharmacistProfileResponse;
import com.medisync.dto.UpdatePharmacistProfileRequest;
import com.medisync.model.User;
import org.springframework.stereotype.Service;

@Service
public class PharmacistProfileService {

    private final PharmacistProfileDao pharmacistProfileDao;
    private final UserDao userDao;

    public PharmacistProfileService(PharmacistProfileDao pharmacistProfileDao, UserDao userDao) {
        this.pharmacistProfileDao = pharmacistProfileDao;
        this.userDao = userDao;
    }

    private Long getUserIdByEmail(String email) {
        User user = userDao.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return user.getUserId();
    }

    public PharmacistProfileResponse getMyProfile(String email) {
        Long userId = getUserIdByEmail(email);
        PharmacistProfileResponse profile = pharmacistProfileDao.getProfileByUserId(userId);
        if (profile == null) {
            throw new IllegalArgumentException("Pharmacist profile not found");
        }
        return profile;
    }

    public PharmacistProfileResponse updateMyProfile(String email, UpdatePharmacistProfileRequest request) {
        Long userId = getUserIdByEmail(email);
        boolean updated = pharmacistProfileDao.updateProfile(userId, request);
        if (!updated) {
            throw new IllegalArgumentException("Failed to update profile or profile not found");
        }
        return pharmacistProfileDao.getProfileByUserId(userId);
    }
}
