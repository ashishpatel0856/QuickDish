package com.ashish.QuickDish.security;

import com.ashish.QuickDish.Entity.Otp;
import com.ashish.QuickDish.Entity.OwnerDocuments;
import com.ashish.QuickDish.Entity.RiderProfile;
import com.ashish.QuickDish.Entity.User;
import com.ashish.QuickDish.Entity.enums.Role;
import com.ashish.QuickDish.Entity.enums.RiderStatus;
import com.ashish.QuickDish.advice.ApiResponse;
import com.ashish.QuickDish.config.OtpGenerator;
import com.ashish.QuickDish.dto.LoginDto;
import com.ashish.QuickDish.dto.OtpRequestDto;
import com.ashish.QuickDish.dto.SignupDto;
import com.ashish.QuickDish.repository.OtpRepository;
import com.ashish.QuickDish.repository.OwnerDocumentsRepository;
import com.ashish.QuickDish.repository.RiderProfileRepository;
import com.ashish.QuickDish.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final RiderProfileRepository riderProfileRepository;
    private final OwnerDocumentsRepository ownerDocumentsRepository;

    @Value("${admin.email}")
    private String ADMIN_EMAIL;

    @Transactional
    public ApiResponse<String> signUp(SignupDto signUpRequestDto) {

        if (userRepository.findByEmail(signUpRequestDto.getEmail()).isPresent()) {
            throw new RuntimeException("User has already registered with this email");
        }

        User newUser = modelMapper.map(signUpRequestDto, User.class);
        newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
        newUser.setIsVerified(false);
        newUser.setIsApproved(false);  // Default not approved

        Role role = Role.ROLE_USER;

        if (signUpRequestDto.getRole() != null && !signUpRequestDto.getRole().isBlank()) {
            Role requestedRole = Role.valueOf(signUpRequestDto.getRole().toUpperCase());

            // Block admin signup
            if (requestedRole == Role.ROLE_ADMIN) {
                throw new RuntimeException("Admin registration not allowed");
            }
            role = requestedRole;
        }

        newUser.setRoles(Set.of(role));

        // rider signup
        if (role == Role.ROLE_RIDER) {
            validateRiderFields(signUpRequestDto);
            RiderProfile riderProfile = new RiderProfile();

            riderProfile.setUser(newUser);
            riderProfile.setVehicleType(signUpRequestDto.getVehicleType());
            riderProfile.setLicenseNumber(signUpRequestDto.getLicenseNumber());
            riderProfile.setVehicleNumber(signUpRequestDto.getVehicleNumber());
            riderProfile.setPhone(signUpRequestDto.getPhone());
            riderProfile.setStatus(RiderStatus.OFFLINE);
            riderProfile.setIsVerifiedRider(false);  // Pending without admin approval
            riderProfile.setRating(0.0);
            riderProfile.setTotalDeliveries(0);

            newUser.setRiderProfile(riderProfile);
        }

        // restaurant signup
        if (role == Role.ROLE_RESTAURANT_OWNER) {
            validateOwnerDocuments(signUpRequestDto);
            OwnerDocuments documents = new OwnerDocuments();

            documents.setUser(newUser);
            documents.setGstNumber(signUpRequestDto.getGstNumber());
            documents.setPanNumber(signUpRequestDto.getPanNumber());
            documents.setFssaiLicense(signUpRequestDto.getFssaiLicense());
            documents.setBusinessRegistrationNumber(signUpRequestDto.getBusinessRegistrationNumber());
            documents.setBankAccountNumber(signUpRequestDto.getBankAccountNumber());
            documents.setIfscCode(signUpRequestDto.getIfscCode());
            documents.setAccountHolderName(signUpRequestDto.getAccountHolderName());
            documents.setBankName(signUpRequestDto.getBankName());

            // Document URLs
            documents.setGstCertificateUrl(signUpRequestDto.getGstCertificateUrl());
            documents.setPanCardUrl(signUpRequestDto.getPanCardUrl());
            documents.setFssaiCertificateUrl(signUpRequestDto.getFssaiCertificateUrl());
            documents.setBankProofUrl(signUpRequestDto.getBankProofUrl());
            documents.setDocumentsUploaded(true);
            documents.setDocumentsVerified(false);  // Pending without admin approval

            newUser.setOwnerDocuments(documents);
            newUser.setIsApproved(false);  // Pending admin approval
        }

        // customer signup
        if (role == Role.ROLE_USER) {
            newUser.setIsApproved(true);  // Auto-approve customers
        }
        userRepository.save(newUser);


        // Generate and send OTP
        String otpValue = OtpGenerator.generateOtp();
        Otp otp = new Otp();
        otp.setEmail(newUser.getEmail());
        otp.setOtp(otpValue);
        otp.setOtpExpiryTime(LocalDateTime.now().plusMinutes(10)); // otp expirations for 10 minutes
        otp.setRole(role.toString());
        otpRepository.save(otp);

        emailService.sendOtpEmail(newUser.getEmail(), otpValue, newUser.getName());
        String message;
        switch (role) {
            case ROLE_RIDER:
                message = "OTP sent to your email. After OTP verification, your rider account will be pending admin approval.";
                break;
            case ROLE_RESTAURANT_OWNER:
                message = "OTP sent to your email. After OTP verification, your documents will be verified by our team within 24-48 hours.";
                break;
            default:
                message = "OTP sent to your email. Please verify to start ordering.";
        }

        return new ApiResponse<>(message);
    }

    private void validateRiderFields(SignupDto dto) {
        if (dto.getVehicleType() == null || dto.getVehicleType().trim().isEmpty()) {
            throw new RuntimeException("Vehicle type is required");
        }
        if (dto.getLicenseNumber() == null || dto.getLicenseNumber().trim().isEmpty()) {
            throw new RuntimeException("License number is required");
        }
        if (dto.getPhone() == null || dto.getPhone().trim().isEmpty()) {
            throw new RuntimeException("Phone Number is required");
        }
    }


    private void validateOwnerDocuments(SignupDto dto) {
        if (dto.getGstNumber() == null || dto.getGstNumber().trim().isEmpty()) {
            throw new RuntimeException("GST number is required for restaurant owners");
        }
        if (dto.getPanNumber() == null || dto.getPanNumber().trim().isEmpty()) {
            throw new RuntimeException("PAN number is required");
        }
        if (dto.getFssaiLicense() == null || dto.getFssaiLicense().trim().isEmpty()) {
            throw new RuntimeException("FSSAI license number is required for food business");
        }
        if (dto.getBankAccountNumber() == null || dto.getIfscCode() == null) {
            throw new RuntimeException("Bank details are required for payouts");
        }
    }

    public ApiResponse<String> VerifyOtp(OtpRequestDto otpRequestDto) {
        Otp otp = otpRepository.findByEmail(otpRequestDto.getEmail())
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (otp.getOtpExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP is expired");
        }

        if (!otp.getOtp().equals(otpRequestDto.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        User user = userRepository.findByEmail(otp.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsVerified(true);
        String message = "OTP verified successfully";

        // for rider otp verification
        if (user.getRoles().contains(Role.ROLE_RIDER)) {
            if (!user.getRiderProfile().getIsVerifiedRider()) {
                userRepository.save(user);
                otpRepository.delete(otp);
                return new ApiResponse<>("OTP verified. Your rider account is pending admin approval. You will receive an email once approved.");
            }
        }
        // for owner otp verifications
        if (user.getRoles().contains(Role.ROLE_RESTAURANT_OWNER)) {
            if (!user.getIsApproved()) {
                userRepository.save(user);
                otpRepository.delete(otp);
                return new ApiResponse<>("OTP verified. Your documents are under review. You will receive an email once verified (24-48 hours).");
            }
        }
        userRepository.save(user);
        otpRepository.delete(otp);
        return new ApiResponse<>(message);
    }



    public String[] login(LoginDto loginDto) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));

        User user = (User) authentication.getPrincipal();

        // Check if account is verified (if otp done)
        if (!user.getIsVerified()) {
            throw new RuntimeException("Account not verified. Please verify OTP first.");
        }

        // Admin check
        if (user.getRoles().contains(Role.ROLE_ADMIN)) {
            if (!user.getEmail().equals(ADMIN_EMAIL)) {
                throw new RuntimeException("Only super admin can login");
            }
        }

        // rider check
        if (user.getRoles().contains(Role.ROLE_RIDER)) {
            if (user.getRiderProfile() == null || !user.getRiderProfile().getIsVerifiedRider()) {
                throw new RuntimeException("Your rider account is pending admin approval. Please wait for approval email.");
            }
        }

        // restaurant approval check
        if (user.getRoles().contains(Role.ROLE_RESTAURANT_OWNER)) {
            if (!user.getIsApproved()) {
                // Check if documents are uploaded
                if (user.getOwnerDocuments() == null || !user.getOwnerDocuments().getDocumentsUploaded()) {
                    throw new RuntimeException("Please upload your business documents to complete registration.");
                }
                if (!user.getOwnerDocuments().getDocumentsVerified()) {
                    throw new RuntimeException("Your documents are under review. Please wait for approval (24-48 hours).");
                }
                throw new RuntimeException("Your restaurant owner account is pending admin approval.");
            }
        }

        String[] tokens = new String[2];
        tokens[0] = jwtService.generateAccessToken(user);
        tokens[1] = jwtService.generateRefreshToken(user);
        return tokens;
    }


    public User getUserFromToken(String token) {
        Long userId = jwtService.getUserIdFromToken(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationServiceException("User not found"));
    }


    public String refreshToken(String refreshToken) {
        Long userId = jwtService.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getIsVerified()) {
            throw new RuntimeException("Account not verified");
        }
        // Same approval checks for refresh
        if (user.getRoles().contains(Role.ROLE_RIDER)) {
            if (user.getRiderProfile() == null || !user.getRiderProfile().getIsVerifiedRider()) {
                throw new RuntimeException("Rider account pending approval");
            }
        }
        if (user.getRoles().contains(Role.ROLE_RESTAURANT_OWNER)) {
            if (!user.getIsApproved()) {
                throw new RuntimeException("Restaurant owner account pending approval");
            }
        }

        return jwtService.generateAccessToken(user);
    }
}