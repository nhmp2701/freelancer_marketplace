package com.freelance.marketplace.controller;

import com.freelance.marketplace.dto.request.CreateJobRequest;
import com.freelance.marketplace.dto.request.UpdateJobRequest;
import com.freelance.marketplace.dto.response.JobResponse;
import com.freelance.marketplace.dto.response.PageResponse;
import com.freelance.marketplace.enums.JobStatus;
import com.freelance.marketplace.service.job.JobCommandService;
import com.freelance.marketplace.service.job.JobQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobCommandService jobCommandService;
    private final JobQueryService jobQueryService;

    // API công khai: tìm kiếm job
    @GetMapping
    public ResponseEntity<PageResponse<JobResponse>> searchJobs(
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) @PositiveOrZero BigDecimal minBudget,
            @RequestParam(required = false) @PositiveOrZero BigDecimal maxBudget,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return ResponseEntity.ok(jobQueryService.searchJobs(
                status, minBudget, maxBudget, skill, keyword, page, size, sortBy, sortDirection));
    }

    // API công khai: xem chi tiết job
    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobQueryService.getJobById(id));
    }

    // Client đăng job
    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            Authentication authentication,
            @Valid @RequestBody CreateJobRequest request) {
        JobResponse response = jobCommandService.createJob(authentication.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Client sửa job
    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobRequest request) {
        return ResponseEntity.ok(jobCommandService.updateJob(authentication.getName(), id, request));
    }

    // Client hủy job
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelJob(
            Authentication authentication,
            @PathVariable Long id) {
        jobCommandService.cancelJob(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    // Client xem job mình đã đăng
    @GetMapping("/me/posted")
    public ResponseEntity<List<JobResponse>> getMyPostedJobs(Authentication authentication) {
        return ResponseEntity.ok(jobQueryService.getMyPostedJobs(authentication.getName()));
    }

    // Freelancer xem job mình đã nhận
    @GetMapping("/me/accepted")
    public ResponseEntity<List<JobResponse>> getMyAcceptedJobs(Authentication authentication) {
        return ResponseEntity.ok(jobQueryService.getMyAcceptedJobs(authentication.getName()));
    }
}
