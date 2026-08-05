package com.ashish.QuickDish.ai.repository;

import com.ashish.QuickDish.ai.entity.AiConversation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {

    List<AiConversation> findBySessionIdOrderByCreatedAtDesc(String sessionId, Pageable pageable);

    List<AiConversation> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}