package com.medisync;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testRegistrationIsPublic() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"email\":\"new@example.com\",\"password\":\"password\",\"firstName\":\"John\",\"lastName\":\"Doe\"}";
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        
        // Test standard path
        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/auth/register", request, String.class);
        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);

        // Test trailing slash
        ResponseEntity<String> responseSlash = restTemplate.postForEntity("/api/v1/auth/register/", request, String.class);
        assertThat(responseSlash.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);

        // Test OPTIONS (Preflight)
        ResponseEntity<String> responseOptions = restTemplate.exchange("/api/v1/auth/register", HttpMethod.OPTIONS, new HttpEntity<>(new HttpHeaders()), String.class);
        assertThat(responseOptions.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
