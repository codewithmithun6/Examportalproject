package com.exam.service;

import com.exam.model.User;
import com.exam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordRecoveryService {
    private static final long OTP_TTL_SECONDS = 600;
    private static final long RESET_TOKEN_TTL_SECONDS = 600;
    private static final long RESEND_WAIT_SECONDS = 60;
    private static final int MAX_ATTEMPTS = 5;

    private final ConcurrentHashMap<String, RecoveryState> recoveries = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public PasswordRecoveryService(UserRepository userRepository,
                                   BCryptPasswordEncoder passwordEncoder,
                                   JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    public void sendOtp(String emailInput) {
        if (emailInput == null || !emailInput.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Enter a valid email address");
        }
        if (mailUsername == null || mailUsername.isBlank()) {
            throw new IllegalStateException("Email service is not configured. Set MAIL_USERNAME and MAIL_APP_PASSWORD.");
        }

        String email = emailInput.trim().toLowerCase(Locale.ROOT);
        List<User> users = userRepository.findAllByEmailIgnoreCase(email);
        if (users.size() != 1) {
            return;
        }

        RecoveryState previous = recoveries.get(email);
        Instant now = Instant.now();
        if (previous != null && previous.lastSentAt.plusSeconds(RESEND_WAIT_SECONDS).isAfter(now)) {
            throw new IllegalStateException("Please wait one minute before requesting another code");
        }

        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        RecoveryState state = new RecoveryState(users.get(0).getId(), passwordEncoder.encode(otp), now.plusSeconds(OTP_TTL_SECONDS), now);
        if (previous != null) {
            state.attempts = previous.attempts;
        }
        recoveries.put(email, state);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailUsername);
            message.setTo(email);
            message.setSubject("Exam Portal password reset code");
            message.setText("Your password reset code is " + otp + ". It expires in 10 minutes.");
            mailSender.send(message);
        } catch (MailException ex) {
            recoveries.remove(email, state);
            throw new IllegalStateException("Unable to send the reset code. Check email settings and try again.", ex);
        } catch (RuntimeException ex) {
            recoveries.remove(email, state);
            throw new IllegalStateException("Unable to send the reset code. Check email settings and try again.", ex);
        }
    }

    public String verifyOtp(String emailInput, String otp) {
        String email = normalizeEmail(emailInput);
        if (otp == null || !otp.matches("\\d{6}")) {
            throw new IllegalArgumentException("Enter the six-digit verification code");
        }

        RecoveryState state = recoveries.get(email);
        if (state == null) {
            throw new IllegalArgumentException("Invalid or expired code. Request a new code.");
        }

        synchronized (state) {
            if (state.otpExpiresAt.isBefore(Instant.now()) || state.attempts >= MAX_ATTEMPTS) {
                recoveries.remove(email, state);
                throw new IllegalArgumentException("Code expired or attempts exceeded. Request a new code.");
            }
            if (!passwordEncoder.matches(otp, state.otpHash)) {
                state.attempts++;
                throw new IllegalArgumentException("Incorrect verification code");
            }

            byte[] tokenBytes = new byte[32];
            secureRandom.nextBytes(tokenBytes);
            String resetToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
            state.resetTokenHash = passwordEncoder.encode(resetToken);
            state.resetTokenExpiresAt = Instant.now().plusSeconds(RESET_TOKEN_TTL_SECONDS);
            state.otpHash = null;
            return resetToken;
        }
    }

    public void resetPassword(String emailInput, String resetToken, String newPassword, String confirmPassword) {
        String email = normalizeEmail(emailInput);
        if (resetToken == null || resetToken.isBlank()) {
            throw new IllegalArgumentException("Verify the email code before resetting your password");
        }
        if (newPassword == null || newPassword.isBlank() || confirmPassword == null || confirmPassword.isBlank()) {
            throw new IllegalArgumentException("Enter and confirm your new password");
        }
        if (newPassword.length() < 8 || newPassword.length() > 72) {
            throw new IllegalArgumentException("Password must be between 8 and 72 characters");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password and confirmation do not match");
        }

        RecoveryState state = recoveries.get(email);
        if (state == null) {
            throw new IllegalArgumentException("Reset session expired. Request a new code.");
        }

        synchronized (state) {
            if (state.resetTokenHash == null || state.resetTokenExpiresAt.isBefore(Instant.now())
                    || !passwordEncoder.matches(resetToken, state.resetTokenHash)) {
                recoveries.remove(email, state);
                throw new IllegalArgumentException("Reset session expired. Request a new code.");
            }

            User user = userRepository.findById(state.userId)
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            recoveries.remove(email, state);
        }
    }

    @Scheduled(fixedDelay = 60_000)
    public void clearExpiredRecoveries() {
        Instant now = Instant.now();
        recoveries.entrySet().removeIf(entry -> {
            RecoveryState state = entry.getValue();
            Instant expiry = state.resetTokenExpiresAt == null ? state.otpExpiresAt : state.resetTokenExpiresAt;
            return expiry.isBefore(now);
        });
    }

    private String normalizeEmail(String email) {
        if (email == null || !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Enter a valid email address");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static final class RecoveryState {
        private final Long userId;
        private String otpHash;
        private final Instant otpExpiresAt;
        private final Instant lastSentAt;
        private int attempts;
        private String resetTokenHash;
        private Instant resetTokenExpiresAt;

        private RecoveryState(Long userId, String otpHash, Instant otpExpiresAt, Instant lastSentAt) {
            this.userId = userId;
            this.otpHash = otpHash;
            this.otpExpiresAt = otpExpiresAt;
            this.lastSentAt = lastSentAt;
        }
    }
}