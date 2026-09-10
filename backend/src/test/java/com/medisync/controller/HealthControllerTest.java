package com.medisync.controller;

import com.medisync.security.SecurityConfig;
import com.medisync.service.DatabaseHealthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
@Import(SecurityConfig.class)
public class HealthControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private DatabaseHealthService databaseHealthService;
    @MockBean private com.medisync.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void testHealthEndpointIsPublic() throws Exception {
        when(databaseHealthService.isDatabaseUp()).thenReturn(true);

        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk());
    }
}
