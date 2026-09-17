package com.freelance.marketplace.repository;

import com.freelance.marketplace.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  boolean existsByJobIdAndReviewerId(Long jobId, Long reviewerId);

  List<Review> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);

  List<Review> findByReviewerIdOrderByCreatedAtDesc(Long reviewerId);
}
