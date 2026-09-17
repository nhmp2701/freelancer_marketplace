package com.freelance.marketplace.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.freelance.marketplace.dto.request.CreateReviewRequest;
import com.freelance.marketplace.entity.*;
import com.freelance.marketplace.enums.JobStatus;
import com.freelance.marketplace.repository.*;
import com.freelance.marketplace.service.review.ReviewService;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ReviewServiceTest {
  @Test
  void rejectsReviewBeforeJobCompletion() {
    UserRepository users = mock(UserRepository.class);
    JobRepository jobs = mock(JobRepository.class);
    ReviewRepository reviews = mock(ReviewRepository.class);
    ReviewService service = new ReviewService(users, jobs, reviews);
    User client = User.builder().id(1L).email("client@example.com").build();
    Job job =
        Job.builder()
            .id(10L)
            .client(client)
            .freelancer(User.builder().id(2L).build())
            .status(JobStatus.IN_PROGRESS)
            .build();
    when(users.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
    when(jobs.findById(job.getId())).thenReturn(Optional.of(job));
    CreateReviewRequest request = new CreateReviewRequest();
    request.setRating(5);

    assertThatThrownBy(() -> service.create(client.getEmail(), job.getId(), request))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("completion");
  }
}
