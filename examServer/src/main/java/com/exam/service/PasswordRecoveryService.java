package com.exam.service;

public interface PasswordRecoveryService {
    void sendOtp(String emailInput);

    String verifyOtp(String emailInput, String otp);

    void resetPassword(String emailInput, String resetToken, String newPassword, String confirmPassword);
}