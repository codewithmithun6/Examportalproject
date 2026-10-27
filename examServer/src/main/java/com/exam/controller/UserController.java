package com.exam.controller;


import com.exam.helper.UserFoundException;
import com.exam.model.User;
import com.exam.passwordhandler.ChangePasswordRequest;
import com.exam.passwordhandler.ProfileUpdateRequest;
import com.exam.registration.RegistrationOtpRequest;
import com.exam.registration.RegistrationRequest;
import com.exam.registration.PasswordRecoveryRequest;
import com.exam.service.PasswordRecoveryService;
import com.exam.service.RegistrationVerificationService;
import com.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/user")
@CrossOrigin("*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RegistrationVerificationService registrationVerificationService;

    @Autowired
    private PasswordRecoveryService passwordRecoveryService;

    @GetMapping("/home")
    public String home(){
        return "Welcome to exam portal";
    }

    @PostMapping("/")
    public ResponseEntity<String> createUserWithoutVerification() {
        return ResponseEntity.status(HttpStatus.GONE)
                .body("Email verification is required. Use the registration verification steps.");
    }

    @PostMapping("/registration/send-otp")
    public ResponseEntity<String> sendRegistrationOtp(@RequestBody RegistrationRequest request) {
        try {
            registrationVerificationService.sendOtp(request);
            return ResponseEntity.ok("Verification code sent to your email address");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
        }
    }

    @PostMapping("/registration/verify-otp")
    public ResponseEntity<String> verifyRegistrationOtp(@RequestBody RegistrationOtpRequest request) {
        try {
            registrationVerificationService.verifyAndRegister(request.getEmail(), request.getOtp());
            return ResponseEntity.ok("Email verified and account registered successfully");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Unable to complete registration. Please request a new code.");
        }
    }

    @PostMapping("/password-recovery/send-otp")
    public ResponseEntity<String> sendPasswordRecoveryOtp(@RequestBody PasswordRecoveryRequest request) {
        try {
            passwordRecoveryService.sendOtp(request.getEmail());
            return ResponseEntity.ok("If an account exists for that email, a reset code has been sent.");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
        }
    }

    @PostMapping("/password-recovery/verify-otp")
    public ResponseEntity<String> verifyPasswordRecoveryOtp(@RequestBody PasswordRecoveryRequest request) {
        try {
            return ResponseEntity.ok(passwordRecoveryService.verifyOtp(request.getEmail(), request.getOtp()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/password-recovery/reset")
    public ResponseEntity<String> resetForgottenPassword(@RequestBody PasswordRecoveryRequest request) {
        try {
            passwordRecoveryService.resetPassword(
                    request.getEmail(), request.getResetToken(), request.getNewPassword(), request.getConfirmPassword());
            return ResponseEntity.ok("Password reset successfully");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // get user by username

    @GetMapping("/{userName}")
    public User getUser(@PathVariable("userName") String userName){

        return this.userService.getUser(userName);
    }

    //delete user by userid

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable("userId") Long userId){
        this.userService.deleteUser(userId);
    }

    // update user
    @PutMapping("/")
    public ResponseEntity<User> updateUser(@RequestBody User user){
        return ResponseEntity.ok(this.userService.updateUser(user));
    }

    // get single user

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable("userId") Long userId){

        return this.userService.getUserById(userId);
    }


    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request, Principal principal) {
        try {
            userService.changePassword(
                    principal.getName(),
                    request.getOldPassword(),
                    request.getNewPassword(),
                    request.getConfirmPassword());
            return ResponseEntity.ok("Password changed successfully");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping(value = "/profile", consumes = "multipart/form-data")
    public ResponseEntity<String> updateCurrentProfile(@ModelAttribute ProfileUpdateRequest request, Principal principal) {
        try {
            userService.updateProfile(principal.getName(), request);
            return ResponseEntity.ok("Profile updated successfully");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to save profile picture");
        }
    }


    @ExceptionHandler(UserFoundException.class)
    public ResponseEntity<?> exceptionHandler(UserFoundException ex) {
        return ResponseEntity.ok(ex.getMessage());
    }
}
