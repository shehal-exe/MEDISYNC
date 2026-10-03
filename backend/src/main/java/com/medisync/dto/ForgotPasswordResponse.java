package com.medisync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForgotPasswordResponse {
    private boolean demoMode;
    private String resetToken;

    public ForgotPasswordResponse() {}

    public ForgotPasswordResponse(boolean demoMode, String resetToken) {
        this.demoMode = demoMode;
        this.resetToken = resetToken;
    }

    public boolean isDemoMode() {
        return demoMode;
    }

    public void setDemoMode(boolean demoMode) {
        this.demoMode = demoMode;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }
}
