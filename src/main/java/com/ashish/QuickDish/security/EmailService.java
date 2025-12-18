package com.ashish.QuickDish.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Profile({"local","prod"})
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("ashishkumarr0856@gmail.com"); // verified brevo email
        message.setTo(toEmail);
        message.setSubject("QuickDish Email Verification OTP");
        message.setText("Your OTP is: " + otp);

        mailSender.send(message);

        System.out.println("OTP email sent to: " + toEmail  + " the otp is "+ otp );
    }
}
