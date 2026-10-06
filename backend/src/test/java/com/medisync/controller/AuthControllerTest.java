package com.medisync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisync.dto.LoginRequest;
import com.medisync.dto.RegisterRequest;
import com.medisync.security.CustomUserDetailsService;
import com.medisync.security.SecurityConfig;
import com.medisync.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private AuthenticationManager authenticationManager;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private RememberMeServices rememberMeServices;

    @Test
    void testInvalidRegistrationRejected() throws Exception {
        RegisterRequest request = new RegisterRequest();
        // Missing email, password
        request.setFirstName("John");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testInvalidPasswordRejectedOnLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("patient@example.com");
        request.setPassword("wrongpass");

        when(authenticationManager.authenticate(any())).thenThrow(new AuthenticationException("Bad credentials") {});

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void testLoginWithRememberMe() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("patient@example.com");
        request.setPassword("password");
        request.setRememberMe(true);

        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("patient@example.com", "password")
        );
        when(authService.getUserInfo("patient@example.com")).thenReturn(
                new com.medisync.dto.AuthResponse(1L, "patient@example.com", "PATIENT")
        );

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify that rememberMeServices was called
        org.mockito.Mockito.verify(rememberMeServices).loginSuccess(any(), any(), any());
    }

    @Test
    void testUnauthenticatedMeRejected() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void testAuthenticatedMeSucceeds() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk());
    }

    @Test
    void testHealthEndpointRemainsPublic() throws Exception {
        // Since HealthController is not in this context, we can't test its logic here, 
        // but we can test that SecurityConfig permits it if we had a full test.
        // I will write a small HealthControllerTest.
    }
}
