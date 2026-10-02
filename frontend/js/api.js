const isLocal = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
// NOTE FOR DEPLOYMENT: Replace PROD_BACKEND_URL with your actual deployed Railway/Render backend URL
const PROD_BACKEND_URL = 'https://medisync-backend-production.up.railway.app';
window.BACKEND_DOMAIN = isLocal ? 'http://localhost:8080' : PROD_BACKEND_URL;

const API_BASE_URL = `${window.BACKEND_DOMAIN}/api/v1`;

function redirectFilePreviewToLocalServer() {
    if (window.location.protocol !== 'file:') {
        return;
    }

    const frontendMarker = '/frontend/';
    const currentPath = window.location.pathname;
    const frontendIndex = currentPath.indexOf(frontendMarker);
    const relativePath = frontendIndex >= 0
        ? currentPath.slice(frontendIndex + frontendMarker.length)
        : 'index.html';

    window.location.replace(`http://localhost:8000/${relativePath}${window.location.hash || ''}`);
}

redirectFilePreviewToLocalServer();

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
        if (response.status === 401 && !endpoint.includes('/auth/login') && !endpoint.includes('/auth/me') && !endpoint.includes('/auth/register')) {
            const isSubdir = window.location.pathname.includes('/patient/') || window.location.pathname.includes('/pharmacist/');
            window.location.href = isSubdir ? '../index.html' : 'index.html';
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
