package com.freelance.marketplace.service.review;

import com.freelance.marketplace.dto.request.CreateReviewRequest;
import com.freelance.marketplace.dto.response.*;
import com.freelance.marketplace.entity.*;
import com.freelance.marketplace.enums.JobStatus;
import com.freelance.marketplace.exception.ResourceNotFoundException;
import com.freelance.marketplace.repository.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {
  private final UserRepository userRepository;
  private final JobRepository jobRepository;
  private final ReviewRepository reviewRepository;

  @Transactional
  /** Tạo đúng một đánh giá của mỗi bên sau khi dự án hoàn thành và tính lại uy tín. */
  public ReviewResponse create(String email, Long jobId, CreateReviewRequest request) {
    User reviewer = user(email);
    Job job =
        jobRepository
            .findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    if (job.getStatus() != JobStatus.COMPLETED)
      throw new IllegalStateException("Reviews are allowed only after completion");
    User reviewee;
    if (job.getClient().getId().equals(reviewer.getId())) reviewee = job.getFreelancer();
    else if (job.getFreelancer() != null && job.getFreelancer().getId().equals(reviewer.getId()))
      reviewee = job.getClient();
    else throw new AccessDeniedException("Only job participants can review");
    if (reviewRepository.existsByJobIdAndReviewerId(jobId, reviewer.getId()))
      throw new IllegalStateException("You already reviewed this job");
    Review saved =
        reviewRepository.save(
            Review.builder()
                .job(job)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .rating(request.getRating())
                .comment(request.getComment())
                .build());
    reviewRepository.flush();
    List<Review> received = reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(reviewee.getId());
    reviewee.setReputationScore(
        BigDecimal.valueOf(
            received.stream().mapToInt(Review::getRating).average().orElse(request.getRating())));
    userRepository.save(reviewee);
    return response(saved);
  }

  @Transactional(readOnly = true)
  public List<ReviewResponse> forUser(Long userId) {
    userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(userId).stream()
        .map(this::response)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ReviewResponse> mine(String email) {
    return reviewRepository.findByReviewerIdOrderByCreatedAtDesc(user(email).getId()).stream()
        .map(this::response)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<FreelancerRankingResponse> rankings(String mode) {
    List<Review> reviews = reviewRepository.findAll();
    Map<Long, List<Review>> byUser =
        reviews.stream().collect(Collectors.groupingBy(r -> r.getReviewee().getId()));
    List<Job> completed = jobRepository.findByStatus(JobStatus.COMPLETED);
    LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
    Set<Long> ids = new HashSet<>(byUser.keySet());
    completed.stream()
        .filter(j -> j.getFreelancer() != null)
        .forEach(j -> ids.add(j.getFreelancer().getId()));
    Map<Long, User> users =
        userRepository.findAllById(ids).stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));
    return ids.stream()
        .map(
            id -> {
              List<Review> userReviews = byUser.getOrDefault(id, List.of());
              long jobs =
                  completed.stream()
                      .filter(
                          j -> j.getFreelancer() != null && j.getFreelancer().getId().equals(id))
                      .count();
              long monthly =
                  completed.stream()
                      .filter(
                          j ->
                              j.getFreelancer() != null
                                  && j.getFreelancer().getId().equals(id)
                                  && j.getUpdatedAt() != null
                                  && !j.getUpdatedAt().isBefore(monthStart))
                      .count();
              BigDecimal avg =
                  userReviews.isEmpty()
                      ? BigDecimal.ZERO
                      : BigDecimal.valueOf(
                          userReviews.stream().mapToInt(Review::getRating).average().orElse(0));
              User u = users.get(id);
              return new Ranked(
                  FreelancerRankingResponse.builder()
                      .userId(id)
                      .fullName(u.getFullName())
                      .avatarUrl(u.getAvatarUrl())
                      .averageRating(avg)
                      .reviewCount(userReviews.size())
                      .completedJobs(jobs)
                      .build(),
                  monthly);
            })
        .filter(r -> mode.equals("monthly") ? r.monthly > 0 : r.value.getReviewCount() > 0)
        .sorted(
            mode.equals("monthly")
                ? Comparator.<Ranked>comparingLong(r -> r.monthly)
                    .reversed()
                    .thenComparing(r -> r.value.getAverageRating(), Comparator.reverseOrder())
                    .thenComparing(r -> r.value.getUserId())
                : Comparator.<Ranked, BigDecimal>comparing(r -> r.value.getAverageRating())
                    .reversed()
                    .thenComparing(r -> r.value.getReviewCount(), Comparator.reverseOrder())
                    .thenComparing(r -> r.value.getUserId()))
        .limit(10)
        .map(r -> r.value)
        .toList();
  }

  private ReviewResponse response(Review r) {
    return ReviewResponse.builder()
        .id(r.getId())
        .jobId(r.getJob().getId())
        .jobTitle(r.getJob().getTitle())
        .reviewerId(r.getReviewer().getId())
        .reviewerName(r.getReviewer().getFullName())
        .revieweeId(r.getReviewee().getId())
        .rating(r.getRating())
        .comment(r.getComment())
        .createdAt(r.getCreatedAt())
        .build();
  }

  private User user(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }

  private record Ranked(FreelancerRankingResponse value, long monthly) {}
}
