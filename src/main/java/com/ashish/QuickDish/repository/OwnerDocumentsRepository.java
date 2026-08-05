package com.ashish.QuickDish.repository;

import com.ashish.QuickDish.Entity.OwnerDocuments;
import com.ashish.QuickDish.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OwnerDocumentsRepository extends JpaRepository<OwnerDocuments, Long> {

    Optional<OwnerDocuments> findByUser(User user);
    long countByDocumentsVerifiedFalse();
    void deleteByUser(User user);
}