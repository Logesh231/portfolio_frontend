package com.example.portfolioapp.model;
public class ResetPasswordRequest {
    public String resetToken;
    public String newPassword;
    public ResetPasswordRequest(String resetToken, String newPassword) {
        this.resetToken  = resetToken;
        this.newPassword = newPassword;
    }
}
