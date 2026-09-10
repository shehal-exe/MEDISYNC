package com.medisync.service;

import com.medisync.dao.PasswordResetTokenDao;
import com.medisync.dao.PatientProfileDao;
import com.medisync.dao.RoleDao;
import com.medisync.dao.UserDao;
import com.medisync.dto.*;
import com.medisync.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthService {

    private final UserDao userDao;
    private final RoleDao roleDao;
    private final PatientProfileDao patientProfileDao;
    private final PasswordResetTokenDao passwordResetTokenDao;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserDao userDao, RoleDao roleDao, PatientProfileDao patientProfileDao, 
                       PasswordResetTokenDao passwordResetTokenDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.patientProfileDao = patientProfileDao;
        this.passwordResetTokenDao = passwordResetTokenDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse registerPatient(RegisterRequest request) {
        if (userDao.findByEmail(request.getEmail()) != null) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        Long userId = userDao.save(user);

        roleDao.addRole(userId, "PATIENT");
        patientProfileDao.createProfile(userId, request.getFirstName(), request.getLastName(), 
                request.getContactNumber(), request.getDateOfBirth());

        return new AuthResponse(userId, user.getEmail(), "PATIENT");
    }

    public AuthResponse getUserInfo(String email) {
        User user = userDao.findByEmail(email);
        if (user == null) return null;
        String role = roleDao.getRoleByUserId(user.getUserId());
        return new AuthResponse(user.getUserId(), user.getEmail(), role);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userDao.findByEmail(email);
        if (user == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid current password");
        }
        userDao.updatePassword(user.getUserId(), passwordEncoder.encode(request.getNewPassword()));
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userDao.findByEmail(request.getEmail());
        if (user != null) {
            String rawToken = UUID.randomUUID().toString();
            String tokenHash = hashToken(rawToken);
            passwordResetTokenDao.createToken(user.getUserId(), tokenHash, LocalDateTime.now().plusHours(1));
            // In a real application, send an email with the rawToken here.
            // For this academic project, we simulate success without emailing.
        }
    }

    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = hashToken(request.getToken());
        Long userId = passwordResetTokenDao.getUserIdByValidTokenHash(tokenHash, LocalDateTime.now());
        if (userId == null) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        userDao.updatePassword(userId, passwordEncoder.encode(request.getNewPassword()));
        passwordResetTokenDao.invalidateToken(tokenHash);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
