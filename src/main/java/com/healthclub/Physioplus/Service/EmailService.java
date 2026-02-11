package com.healthclub.Physioplus.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    private boolean isConfigured() {
        return mailSender != null && fromEmail != null && !fromEmail.isBlank();
    }

    public boolean sendOtp(String toEmail, String otp) {
        if (!isConfigured()) {
            System.out.println("[DEV MODE] Email not configured. OTP for " + toEmail + ": " + otp);
            return true;  // Return true in dev mode to allow testing
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("PhysioPlus - Your OTP Code");
            message.setText(buildOtpEmailContent(otp));
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
            return false;
        }
    }

    private String buildOtpEmailContent(String otp) {
        return String.format("""
            Welcome to PhysioPlus!

            Your One-Time Password (OTP) is: %s

            This OTP is valid for 5 minutes. Do not share it with anyone.

            If you did not request this OTP, please ignore this email.

            Regards,
            PhysioPlus Team
            """, otp);
    }

    public boolean sendAppointmentReminder(String toEmail, String patientName, String doctorName, String appointmentTime) {
        if (!isConfigured()) {
            System.out.println("[DEV MODE] Email not configured. Appointment reminder for " + toEmail);
            return true;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("PhysioPlus - Appointment Reminder");
            message.setText(String.format("""
                Dear %s,

                This is a reminder for your upcoming appointment:

                Doctor: %s
                Time: %s

                Please be ready 5 minutes before the scheduled time.

                Regards,
                PhysioPlus Team
                """, patientName, doctorName, appointmentTime));
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send appointment reminder: " + e.getMessage());
            return false;
        }
    }
}
