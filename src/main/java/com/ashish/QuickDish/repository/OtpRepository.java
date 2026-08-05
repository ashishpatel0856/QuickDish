package com.ashish.QuickDish.repository;

import com.ashish.QuickDish.Entity.Otp;
import com.ashish.QuickDish.dto.VerifiedDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByEmail(String email);
}
