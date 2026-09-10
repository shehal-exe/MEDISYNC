package com.medisync.service;

import com.medisync.dao.PasswordResetTokenDao;
import com.medisync.dao.PatientProfileDao;
import com.medisync.dao.RoleDao;
import com.medisync.dao.UserDao;
import com.medisync.dto.*;
import com.medisync.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock private UserDao userDao;
    @Mock private RoleDao roleDao;
    @Mock private PatientProfileDao patientProfileDao;
    @Mock private PasswordResetTokenDao passwordResetTokenDao;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AuthService authService;

    private User dummyUser;

    @BeforeEach
    void setUp() {
        dummyUser = new User();
        dummyUser.setUserId(1L);
        dummyUser.setEmail("patient@example.com");
        dummyUser.setPasswordHash("hashed_password");
    }

    @Test
    void testSuccessfulPatientRegistration() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("password");
        request.setFirstName("John");
        request.setLastName("Doe");

        when(userDao.findByEmail(request.getEmail())).thenReturn(null);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_pass");
        when(userDao.save(any(User.class))).thenReturn(2L);

        AuthResponse response = authService.registerPatient(request);

        assertNotNull(response);
        assertEquals(2L, response.getUserId());
        assertEquals("PATIENT", response.getRole());
        verify(roleDao).addRole(2L, "PATIENT");
        verify(patientProfileDao).createProfile(eq(2L), eq("John"), eq("Doe"), any(), any());
    }

    @Test
    void testDuplicateEmailRejected() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("patient@example.com");

        when(userDao.findByEmail(request.getEmail())).thenReturn(dummyUser);

        assertThrows(IllegalArgumentException.class, () -> authService.registerPatient(request));
        verify(userDao, never()).save(any());
    }

    @Test
    void testChangePasswordSuccess() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPass");
        request.setNewPassword("newPass");

        when(userDao.findByEmail(dummyUser.getEmail())).thenReturn(dummyUser);
        when(passwordEncoder.matches("oldPass", "hashed_password")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("new_hashed");

        authService.changePassword(dummyUser.getEmail(), request);

        verify(userDao).updatePassword(dummyUser.getUserId(), "new_hashed");
    }

    @Test
    void testChangePasswordInvalidCurrentPasswordRejected() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPass");
        request.setNewPassword("newPass");

        when(userDao.findByEmail(dummyUser.getEmail())).thenReturn(dummyUser);
        when(passwordEncoder.matches("wrongPass", "hashed_password")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.changePassword(dummyUser.getEmail(), request));
    }

    @Test
    void testForgotPasswordDoesNotRevealExistence() {
        ForgotPasswordRequest req1 = new ForgotPasswordRequest();
        req1.setEmail("exists@example.com");
        when(userDao.findByEmail("exists@example.com")).thenReturn(dummyUser);
        
        ForgotPasswordRequest req2 = new ForgotPasswordRequest();
        req2.setEmail("notexists@example.com");
        when(userDao.findByEmail("notexists@example.com")).thenReturn(null);

        assertDoesNotThrow(() -> authService.forgotPassword(req1));
        assertDoesNotThrow(() -> authService.forgotPassword(req2));
        
        verify(passwordResetTokenDao, times(1)).createToken(any(), any(), any());
    }

    @Test
    void testValidPasswordReset() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("rawToken");
        request.setNewPassword("newPass");

        when(passwordResetTokenDao.getUserIdByValidTokenHash(anyString(), any(LocalDateTime.class))).thenReturn(1L);
        when(passwordEncoder.encode("newPass")).thenReturn("new_hashed");

        authService.resetPassword(request);

        verify(userDao).updatePassword(1L, "new_hashed");
        verify(passwordResetTokenDao).invalidateToken(anyString());
    }

    @Test
    void testExpiredOrConsumedPasswordResetRejected() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("invalidToken");
        request.setNewPassword("newPass");

        when(passwordResetTokenDao.getUserIdByValidTokenHash(anyString(), any(LocalDateTime.class))).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(request));
        verify(userDao, never()).updatePassword(any(), any());
    }
}
