package com.ashish.QuickDish.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Profile({"local","prod"})
public class EmailService {

    private final JavaMailSender mailSender;
    public  void sendOtpEmail(String toEmail , String otp) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("QuickDish email verification otp:");
        message.setText("OTP verification sent to your email :"+otp);
        mailSender.send(message); // yha se message send hota h


//        System.out.println("OTP verification email has been sent to "+toEmail); // ye console me message display krta h


    }
}
