package com.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for handling email communications in the authentication system.
 * 
 * This service provides functionality to send system emails, particularly for password reset
 * operations where One-Time Passwords (OTP) need to be delivered to users. It uses Spring's
 * JavaMailSender for handling email operations.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Sends an email containing a One-Time Password (OTP) to the specified email address.
     * The email includes the OTP and information about its expiration time.
     *
     * @param to the recipient's email address
     * @param otp the generated OTP to be sent
     * @throws MessagingException if there is an error in creating or sending the email
     */
    public void sendOtpEmail(String to, String otp) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("noreply@authservice.com");
        helper.setTo(to);
        helper.setSubject("Password Reset OTP");
        helper.setText(String.format(
                "Your OTP for password reset is: %s\n\n" +
                "This OTP will expire in 5 minutes.\n" +
                "If you didn't request this, please ignore this email.", 
                otp
        ));

        mailSender.send(message);
    }
}