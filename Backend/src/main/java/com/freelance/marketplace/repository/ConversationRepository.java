package com.freelance.marketplace.repository;

import com.freelance.marketplace.entity.Conversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
  Optional<Conversation> findByJobId(Long jobId);

  @Query(
      "select c from Conversation c where c.job.client.id = :userId or c.job.freelancer.id ="
          + " :userId order by c.createdAt desc")
  List<Conversation> findForParticipant(Long userId);
}
