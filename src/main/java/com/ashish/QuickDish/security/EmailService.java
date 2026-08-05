package com.ashish.QuickDish.security;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${admin.email}")
    private String ADMIN_EMAIL;
    // registration for new users
       public void sendOtpEmail(String toEmail, String otp, String userName) {
      try {
           Context context = new Context();
            context.setVariable("otp", otp);
            context.setVariable("userName", userName);
            context.setVariable("logoCid", "logo");
            String htmlContent = templateEngine.process("email/otp", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper msg = new MimeMessageHelper(message, true, "UTF-8");

            msg.setFrom(ADMIN_EMAIL);
            msg.setTo(toEmail);
            msg.setSubject("QuickDish Email Verification");
            msg.setText(htmlContent, true);
          msg.addInline("logo", new ClassPathResource("static/images/QD.png"), "image/png");
          mailSender.send(message);

        } catch (Exception e) {
          e.printStackTrace();
            throw new RuntimeException("Failed to send email", e);
        }
    }

//    public void sendOtpEmail(String toEmail, String otp) {
//        sendOtpEmail(toEmail, otp, "User");
//    }






    //pickup otp to riders
    public void sendPickupOtp(String toEmail, String riderName, String pickupOtp,
                              String restaurantName, String restaurantAddress) {
        try {
            Context context = new Context();
            context.setVariable("riderName", riderName);
            context.setVariable("pickupOtp", pickupOtp);
            context.setVariable("restaurantName", restaurantName);
            context.setVariable("restaurantAddress", restaurantAddress);

            String htmlContent = templateEngine.process("email/rider-pickup-otp", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper msg = new MimeMessageHelper(message, true, "UTF-8");

            msg.setFrom(ADMIN_EMAIL);
            msg.setTo(toEmail);
            msg.setSubject(" Your Pickup OTP  QuickDish");
            msg.setText(htmlContent, true);
            msg.addInline("logo", new ClassPathResource("static/images/QD.png"), "image/png");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send pickup OTP: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // delivery otp to customer
    public void sendDeliveryOtp(String toEmail, String customerName, String deliveryOtp,
                                String riderName, String riderPhone) {
        try {
            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("deliveryOtp", deliveryOtp);
            context.setVariable("riderName", riderName);
            context.setVariable("riderPhone", riderPhone);

            String htmlContent = templateEngine.process("email/customer-delivery-otp", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper msg = new MimeMessageHelper(message, true, "UTF-8");

            msg.setFrom(ADMIN_EMAIL);
            msg.setTo(toEmail);
            msg.setSubject("Your Delivery OTP  QuickDish");
            msg.setText(htmlContent, true);
            msg.addInline("logo", new ClassPathResource("static/images/QD.png"), "image/png");
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println(" Failed to send delivery OTP: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // rider arrived  to restaurant
    public void sendRiderArrivedAtRestaurant(String toEmail, String restaurantName,
                                             String riderName, String riderPhone,
                                             Long orderId, String pickupOtp) {
        try {
            Context context = new Context();
            context.setVariable("restaurantName", restaurantName);
            context.setVariable("riderName", riderName);
            context.setVariable("riderPhone", riderPhone);
            context.setVariable("orderId", orderId);
            context.setVariable("pickupOtp", pickupOtp);

            String htmlContent = templateEngine.process("email/rider-arrived-restaurant", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper msg = new MimeMessageHelper(message, true, "UTF-8");

            msg.setFrom(ADMIN_EMAIL);
            msg.setTo(toEmail);
            msg.setSubject(" Rider Arrived Order " + orderId);
            msg.setText(htmlContent, true);
            msg.addInline("logo", new ClassPathResource("static/images/QD.png"), "image/png");
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Failed to send arrival email: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // rider arrived at customers
    public void sendRiderArrivedAtCustomer(String toEmail, String customerName,
                                           String riderName, String riderPhone,
                                           Long orderId, String deliveryOtp) {
        try {
            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("riderName", riderName);
            context.setVariable("riderPhone", riderPhone);
            context.setVariable("orderId", orderId);
            context.setVariable("deliveryOtp", deliveryOtp);

            String htmlContent = templateEngine.process("email/rider-arrived-customer", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper msg = new MimeMessageHelper(message, true, "UTF-8");

            msg.setFrom(ADMIN_EMAIL);
            msg.setTo(toEmail);
            msg.setSubject(" Your Rider Has Arrived!  Order #" + orderId);
            msg.setText(htmlContent, true);
            msg.addInline("logo", new ClassPathResource("static/images/QD.png"), "image/png");
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println(" Failed to send arrival email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}