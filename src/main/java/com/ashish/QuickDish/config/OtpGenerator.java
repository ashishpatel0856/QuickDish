package com.ashish.QuickDish.config;


import java.util.Random;

public class OtpGenerator {
    public static String generateOtp() {

        Random randomOtp = new Random();
        int otp = 100000 + randomOtp.nextInt(900000);// 6 digits otp generate
        return String.valueOf(otp);
    }
}
