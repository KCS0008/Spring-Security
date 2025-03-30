package com.auth.service;

import com.auth.dto.AuthResponse;
import com.auth.entity.User;
import com.auth.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;

/**
 * Service class handling all authentication-related operations including user registration,
 * login authentication, password reset, and security validation.
 *
 * This service integrates with Spring Security for authentication and uses JWT for token-based
 * authentication. It also provides functionality for OTP-based password reset via email.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${otp.expiration}")
    private long otpExpirationMs;

    /**
     * Registers a new user in the system.
     *
     * @param user the user entity containing registration details
     * @return the saved user entity with encoded password
     * @throws RuntimeException if the email is already registered
     */
    public User register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        return userRepository.save(user);
    }

    /**
     * Authenticates a user and generates JWT tokens.
     *
     * @param email user's email address
     * @param password user's password
     * @return AuthResponse containing JWT access token and refresh token
     * @throws RuntimeException if user is not found or credentials are invalid
     */
    public AuthResponse authenticate(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true, true, true,
                java.util.Collections.emptyList()
        );

        String jwtToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .message("Authentication successful")
                .build();
    }

    /**
     * Initiates the password reset process by generating and sending OTP.
     *
     * @param email email address of the user requesting password reset
     * @throws MessagingException if there's an error sending the email
     * @throws RuntimeException if user is not found
     */
    public void initiatePasswordReset(String email) throws MessagingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = generateOTP();
        user.setOtp(otp);
        user.setOtpExpirationTime(Instant.now().toEpochMilli() + otpExpirationMs);
        userRepository.save(user);

        emailService.sendOtpEmail(email, otp);
    }

    /**
     * Resets user's password after validating OTP.
     *
     * @param email user's email address
     * @param otp one-time password received via email
     * @param newPassword new password to be set
     * @throws RuntimeException if OTP is invalid or expired
     */
    public void resetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!isOtpValid(user, otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setOtp(null);
        user.setOtpExpirationTime(null);
        userRepository.save(user);
    }

    /**
     * Validates user's security answer during password reset process.
     *
     * @param email user's email address
     * @param securityAnswer answer to user's security question
     * @throws RuntimeException if security answer is invalid or user not found
     */
    public void validateSecurityAnswer(String email, String securityAnswer) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getSecurityAnswer().equals(securityAnswer)) {
            throw new RuntimeException("Invalid security answer");
        }
    }

    /**
     * Generates a 6-digit secure random OTP.
     *
     * @return generated OTP as string
     */
    private String generateOTP() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Validates if the provided OTP is valid and not expired.
     *
     * @param user user entity containing stored OTP
     * @param otp OTP to validate
     * @return true if OTP is valid and not expired, false otherwise
     */
    private boolean isOtpValid(User user, String otp) {
        return user.getOtp() != null &&
               user.getOtp().equals(otp) &&
               user.getOtpExpirationTime() != null &&
               Instant.now().toEpochMilli() < user.getOtpExpirationTime();
    }
}