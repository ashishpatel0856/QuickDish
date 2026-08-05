package com.ashish.QuickDish.dto;
import lombok.*;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupDto {

    private Long id;
    private String name;
    private String email;
    private String password;
    private String role;

//    RIDER SPECIFIC FIELD
    private String vehicleType;
    private String licenseNumber;
    private String vehicleNumber;
    private String phone;


    // Restaurant Owner - Business Documents
    private String gstNumber;
    private String panNumber;
    private String fssaiLicense;
    private String businessRegistrationNumber;

    // Bank Details
    private String bankAccountNumber;
    private String ifscCode;
    private String accountHolderName;
    private String bankName;

    // Document URLs (after file upload)
    private String gstCertificateUrl;
    private String panCardUrl;
    private String fssaiCertificateUrl;
    private String bankProofUrl;


}
