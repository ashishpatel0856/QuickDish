package com.ashish.QuickDish.Entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "owner_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerDocuments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // Business Documents
    private String gstNumber;
    private String panNumber;
    private String fssaiLicense;  // Food Safety License
    private String businessRegistrationNumber;

    // Bank Details
    private String bankAccountNumber;
    private String ifscCode;
    private String accountHolderName;
    private String bankName;

    private String gstCertificateUrl;
    private String panCardUrl;
    private String fssaiCertificateUrl;
    private String bankProofUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean documentsUploaded = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean documentsVerified = false;

    private String rejectionReason;  // If admin rejects documents

    @Column(name = "uploaded_at")
    @Builder.Default
    private java.time.LocalDateTime uploadedAt = java.time.LocalDateTime.now();

    public Boolean getDocumentsUploaded() {
        return documentsUploaded;
    }

    public void setDocumentsUploaded(Boolean documentsUploaded) {
        this.documentsUploaded = documentsUploaded;
    }

    public Boolean getDocumentsVerified() {
        return documentsVerified;
    }

    public void setDocumentsVerified(Boolean documentsVerified) {
        this.documentsVerified = documentsVerified;
    }
}