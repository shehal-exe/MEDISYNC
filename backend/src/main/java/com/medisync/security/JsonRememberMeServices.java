package com.medisync.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

public class JsonRememberMeServices extends TokenBasedRememberMeServices {

    public JsonRememberMeServices(String key, UserDetailsService userDetailsService) {
        super(key, userDetailsService);
        setParameter("rememberMe");
    }

    @Override
    protected boolean rememberMeRequested(HttpServletRequest request, String parameter) {
        Boolean rememberMe = (Boolean) request.getAttribute("rememberMe");
        if (rememberMe != null) {
            return rememberMe;
        }
        return super.rememberMeRequested(request, parameter);
    }
}
