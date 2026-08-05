package com.ashish.QuickDish.repository;

import com.ashish.QuickDish.Entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAddressRepository  extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUserId(Long userId);
}
