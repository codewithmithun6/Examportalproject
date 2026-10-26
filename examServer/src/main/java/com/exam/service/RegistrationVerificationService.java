package com.exam.service;

import com.exam.model.Role;
import com.exam.model.User;
import com.exam.model.UserRole;
import com.exam.registration.RegistrationRequest;
import com.exam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistrationVerificationService {
    private static final long OTP_LIFETIME_SECONDS = 600;
    private static final int MAX_OTP_ATTEMPTS = 5;

    private final ConcurrentHashMap<String, PendingRegistration> pendingRegistrations = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private final UserRepository userRepository;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public RegistrationVerificationService(
            UserRepository userRepository,
            UserService userService,
            BCryptPasswordEncoder passwordEncoder,
            JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    public void sendOtp(RegistrationRequest request) {
        validate(request);
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUserName(request.getUserName().trim())) {
            throw new IllegalArgumentException("That username is already registered");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("That email address is already registered");
        }
        if (mailUsername == null || mailUsername.isBlank()) {
            throw new IllegalStateException("Email service is not configured. Set MAIL_USERNAME and MAIL_APP_PASSWORD.");
        }

        User user = new User();
        user.setUserName(request.getUserName().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(email);
        user.setPhone(request.getPhone().trim());
        user.setProfile("default.png");

        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        PendingRegistration pending = new PendingRegistration(user, passwordEncoder.encode(otp), Instant.now().plusSeconds(OTP_LIFETIME_SECONDS));
        pendingRegistrations.put(email, pending);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailUsername);
            message.setTo(email);
            message.setSubject("Exam Portal email verification");
            message.setText("Your verification code is " + otp + ". It expires in 10 minutes.");
            mailSender.send(message);
        } catch (MailException ex) {
            pendingRegistrations.remove(email, pending);
            throw new IllegalStateException("Unable to send verification email. Check SMTP settings and try again.", ex);
        }
    }

    public User verifyAndRegister(String emailInput, String otp) throws Exception {
        if (emailInput == null || emailInput.isBlank() || otp == null || !otp.matches("\\d{6}")) {
            throw new IllegalArgumentException("Enter the email address and six-digit verification code");
        }

        String email = emailInput.trim().toLowerCase(Locale.ROOT);
        PendingRegistration pending = pendingRegistrations.get(email);
        if (pending == null) {
            throw new IllegalArgumentException("No active verification request. Request a new code.");
        }

        synchronized (pending) {
            if (pending.expiresAt().isBefore(Instant.now())) {
                pendingRegistrations.remove(email, pending);
                throw new IllegalArgumentException("Verification code expired. Request a new code.");
            }
            if (pending.attempts() >= MAX_OTP_ATTEMPTS) {
                pendingRegistrations.remove(email, pending);
                throw new IllegalArgumentException("Too many incorrect attempts. Request a new code.");
            }
            if (!passwordEncoder.matches(otp, pending.otpHash())) {
                pending.incrementAttempts();
                throw new IllegalArgumentException("Incorrect verification code");
            }
                if (userRepository.existsByUserName(pending.user().getUserName())
                    || userRepository.existsByEmailIgnoreCase(email)) {
                pendingRegistrations.remove(email, pending);
                throw new IllegalArgumentException("Username or email was registered while verification was pending");
            }

            Role role = new Role();
            role.setRoleId(45L);
            role.setRoleName("NORMAL");
            UserRole userRole = new UserRole();
            userRole.setUser(pending.user());
            userRole.setRole(role);
            Set<UserRole> roles = new HashSet<>();
            roles.add(userRole);

            User registered = userService.createUser(pending.user(), roles);
            pendingRegistrations.remove(email, pending);
            return registered;
        }
    }

    @Scheduled(fixedDelay = 60_000)
    public void removeExpiredRegistrations() {
        Instant now = Instant.now();
        pendingRegistrations.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private void validate(RegistrationRequest request) {
        if (request == null
                || blank(request.getUserName()) || blank(request.getPassword())
                || blank(request.getFirstName()) || blank(request.getLastName())
                || blank(request.getEmail()) || blank(request.getPhone())) {
            throw new IllegalArgumentException("All registration fields are required");
        }
        if (!request.getEmail().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Enter a valid email address");
        }
        if (request.getUserName().trim().length() > 100 || request.getPassword().length() > 72
                || request.getFirstName().trim().length() > 100 || request.getLastName().trim().length() > 100
                || request.getEmail().trim().length() > 150 || request.getPhone().trim().length() > 25) {
            throw new IllegalArgumentException("One or more registration fields are too long");
        }
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static final class PendingRegistration {
        private final User user;
        private final String otpHash;
        private final Instant expiresAt;
        private int attempts;

        private PendingRegistration(User user, String otpHash, Instant expiresAt) {
            this.user = user;
            this.otpHash = otpHash;
            this.expiresAt = expiresAt;
        }

        private User user() { return user; }
        private String otpHash() { return otpHash; }
        private Instant expiresAt() { return expiresAt; }
        private int attempts() { return attempts; }
        private void incrementAttempts() { attempts++; }
    }
}