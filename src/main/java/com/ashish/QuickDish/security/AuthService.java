package com.ashish.QuickDish.security;

import com.ashish.QuickDish.Entity.Otp;
import com.ashish.QuickDish.Entity.User;
import com.ashish.QuickDish.Entity.enums.Role;
import com.ashish.QuickDish.advice.ApiResponse;
import com.ashish.QuickDish.config.OtpGenerator;
import com.ashish.QuickDish.dto.LoginDto;
import com.ashish.QuickDish.dto.OtpRequestDto;
import com.ashish.QuickDish.dto.SignupDto;
import com.ashish.QuickDish.repository.OtpRepository;
import com.ashish.QuickDish.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final OtpRepository otpRepository;
    private final EmailService emailService;


    // signup
    public ApiResponse<String> signUp(SignupDto signUpRequestDto) {

        if (userRepository.findByEmail(signUpRequestDto.getEmail()).isPresent()) {
            throw new RuntimeException("User already registered with this email");
        }

        User newUser = modelMapper.map(signUpRequestDto, User.class);
        newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
        newUser.setIsVerified(false); // OTP not verified yet

        try {
            Role role = Role.valueOf(signUpRequestDto.getRole().toUpperCase());
            newUser.setRole(Set.of(role));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + signUpRequestDto.getRole());
        }

        userRepository.save(newUser);

        // Generate OTP
        String otpValue = OtpGenerator.generateOtp();
        Otp otp = new Otp();
        otp.setEmail(newUser.getEmail());
        otp.setOtp(otpValue);
        otp.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));

        otpRepository.save(otp);

        // Send OTP Email
        emailService.sendOtpEmail(newUser.getEmail(), otpValue);

        return new ApiResponse<>("OTP sent to your email. Please verify to login.");
    }

    // verify otp
    public ApiResponse<String> VerifyOtp(OtpRequestDto otpRequestDto) {

        Otp otp = otpRepository.findByEmail(otpRequestDto.getEmail())
                .orElseThrow(() -> new RuntimeException("OTP not found for this email"));

        if (otp.getOtpExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        if (!otp.getOtp().equals(otpRequestDto.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        User user = userRepository.findByEmail(otp.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsVerified(true);
        userRepository.save(user);

        otpRepository.delete(otp);

        return new ApiResponse<>("OTP verified successfully. You can now login.");
    }

    // login
    public String[] login(LoginDto loginDto) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginDto.getEmail(),
                                loginDto.getPassword()
                        )
                );

        User user = (User) authentication.getPrincipal();

        //BLOCK LOGIN IF OTP NOT VERIFIED
        if (!user.getIsVerified()) {
            throw new RuntimeException("Account not verified. Please verify OTP first.");
        }

        String[] tokens = new String[2];
        tokens[0] = jwtService.generateAccessToken(user);
        tokens[1] = jwtService.generateRefreshToken(user);

        return tokens;
    }

    // refersh
    public String refreshToken(String refreshToken) {

        Long userId = jwtService.getUserIdFromToken(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getIsVerified()) {
            throw new RuntimeException("Account not verified");
        }

        return jwtService.generateAccessToken(user);
    }
}
