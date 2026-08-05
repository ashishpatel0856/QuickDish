package com.ashish.QuickDish.repository;

import com.ashish.QuickDish.Entity.User;
import com.ashish.QuickDish.Entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

   Optional<User> findByEmail(String email);
    List<User> findByRolesContainingAndIsApprovedFalseAndIsVerifiedTrue(Role role);
    List<User> findByRolesContainingAndIsApprovedTrue(Role role);
    long countByRolesContainingAndIsApprovedFalseAndIsVerifiedTrue(Role role);
    long countByRolesContainingAndIsApprovedTrue(Role role);
    long countByRolesContaining(Role role);

}
