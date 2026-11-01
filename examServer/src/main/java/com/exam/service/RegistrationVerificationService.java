package com.exam.service;

import com.exam.model.User;
import com.exam.registration.RegistrationRequest;

public interface RegistrationVerificationService {
    void sendOtp(RegistrationRequest request);

    User verifyAndRegister(String emailInput, String otp) throws Exception;
}