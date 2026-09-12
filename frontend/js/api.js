const API_BASE_URL = 'http://localhost:8080/api/v1';

/**
 * Core fetch wrapper that automatically handles credentials (cookies)
 * and basic JSON parsing/error handling.
 */
async function fetchApi(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    
    const config = {
        ...options,
        // MUST include credentials to send/receive JSESSIONID cookie
        credentials: 'include', 
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        }
    };

    try {
        const response = await fetch(url, config);
        
        // Handle 401 Unauthorized globally (e.g. session expired)
        if (response.status === 401 && !endpoint.includes('/auth/login') && !endpoint.includes('/auth/me')) {
            window.location.href = '/login.html';
            return null;
        }

        const data = await response.json();
        return data;
    } catch (error) {
        console.error('API Error:', error);
        return { success: false, message: 'Network error or server is unreachable.' };
    }
}

// Ensure the frontend doesn't rely on strict CORS if we run files via file:// protocol.
// Note: In production, the backend handles CORS. For local testing, opening HTML files directly 
// in the browser (file://) might cause CORS issues with cookies. It is recommended to use a simple local server (like Live Server or http-server).
