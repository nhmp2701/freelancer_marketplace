package com.freelance.marketplace.controller;

import com.freelance.marketplace.dto.request.CreateReviewRequest;
import com.freelance.marketplace.dto.response.*;
import com.freelance.marketplace.service.review.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewController {
  private final ReviewService service;

  @PostMapping("/jobs/{jobId}/reviews")
  public ResponseEntity<ReviewResponse> create(
      Authentication a, @PathVariable Long jobId, @Valid @RequestBody CreateReviewRequest r) {
    return new ResponseEntity<>(service.create(a.getName(), jobId, r), HttpStatus.CREATED);
  }

  @GetMapping("/users/{userId}/reviews")
  public ResponseEntity<List<ReviewResponse>> reviews(@PathVariable Long userId) {
    return ResponseEntity.ok(service.forUser(userId));
  }

  @GetMapping("/reviews/me")
  public ResponseEntity<List<ReviewResponse>> mine(Authentication a) {
    return ResponseEntity.ok(service.mine(a.getName()));
  }

  @GetMapping("/rankings/freelancers")
  public ResponseEntity<List<FreelancerRankingResponse>> rankings(
      @RequestParam(defaultValue = "trusted") String mode) {
    if (!mode.equals("trusted") && !mode.equals("monthly"))
      throw new IllegalArgumentException("Unsupported ranking mode");
    return ResponseEntity.ok(service.rankings(mode));
  }
}
