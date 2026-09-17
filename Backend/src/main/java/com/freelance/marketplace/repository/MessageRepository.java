package com.freelance.marketplace.repository;

import com.freelance.marketplace.entity.Message;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
  Page<Message> findByConversationId(Long conversationId, Pageable pageable);

  long countByConversationIdAndSenderIdNotAndCreatedAtAfter(
      Long conversationId, Long senderId, java.time.LocalDateTime readAt);

  long countByConversationIdAndSenderIdNot(Long conversationId, Long senderId);

  Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(Long conversationId);
}
