package com.medisync;

import com.medisync.controller.AuthController;
import com.medisync.dto.AuthResponse;
import com.medisync.security.CustomUserDetailsService;
import com.medisync.security.SecurityConfig;
import com.medisync.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private RememberMeServices rememberMeServices;

    @Test
    public void testRegistrationIsPublic() throws Exception {
        String email = "public-register-" + System.nanoTime() + "@example.com";
        when(authService.registerPatient(any())).thenReturn(new AuthResponse(1L, email, "PATIENT"));

        String body = """
                {
                  "email": "%s",
                  "password": "password",
                  "firstName": "John",
                  "lastName": "Doe"
                }
                """.formatted(email);
        
        // Test standard path
        MvcResult response = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andReturn();
        assertThat(response.getResponse().getStatus()).isNotEqualTo(HttpStatus.UNAUTHORIZED.value());

        // Test trailing slash
        MvcResult responseSlash = mockMvc.perform(post("/api/v1/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andReturn();
        assertThat(responseSlash.getResponse().getStatus()).isNotEqualTo(HttpStatus.UNAUTHORIZED.value());

        // Test OPTIONS (Preflight)
        MvcResult responseOptions = mockMvc.perform(options("/api/v1/auth/register")
                .header("Origin", "http://localhost:8000")
                .header("Access-Control-Request-Method", "POST"))
                .andReturn();
        assertThat(responseOptions.getResponse().getStatus()).isNotEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}
