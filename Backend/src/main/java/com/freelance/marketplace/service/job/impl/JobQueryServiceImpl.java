package com.freelance.marketplace.service.job.impl;

import com.freelance.marketplace.dto.response.JobResponse;
import com.freelance.marketplace.dto.response.PageResponse;
import com.freelance.marketplace.entity.Job;
import com.freelance.marketplace.entity.User;
import com.freelance.marketplace.enums.JobStatus;
import com.freelance.marketplace.exception.ResourceNotFoundException;
import com.freelance.marketplace.repository.JobRepository;
import com.freelance.marketplace.repository.UserRepository;
import com.freelance.marketplace.repository.specification.JobSpecification;
import com.freelance.marketplace.service.job.JobMapper;
import com.freelance.marketplace.service.job.JobQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobQueryServiceImpl implements JobQueryService {
    private static final java.util.Set<String> ALLOWED_SORT_FIELDS =
            java.util.Set.of("createdAt", "updatedAt", "budget", "deadline", "title");
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobMapper jobMapper;

    @Override
    public JobResponse getJobById(Long jobId) {
        return jobMapper.toResponse(jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId)));
    }

    @Override
    public PageResponse<JobResponse> searchJobs(JobStatus status, BigDecimal minBudget, BigDecimal maxBudget,
                                                String skill, String keyword, int page, int size,
                                                String sortBy, String sortDirection) {
        if (minBudget != null && maxBudget != null && minBudget.compareTo(maxBudget) > 0) {
            throw new IllegalArgumentException("minBudget must not exceed maxBudget");
        }
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Unsupported job sort field");
        }
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Sort direction must be asc or desc", exception);
        }

        Specification<Job> spec = Specification.where(JobSpecification.hasStatus(status))
                .and(JobSpecification.hasMinBudget(minBudget)).and(JobSpecification.hasMaxBudget(maxBudget))
                .and(JobSpecification.hasSkill(skill)).and(JobSpecification.hasTitleContaining(keyword));
        return PageResponse.from(
                jobRepository.findAll(spec, PageRequest.of(page, size, Sort.by(direction, sortBy))),
                jobMapper::toResponse);
    }

    @Override
    public List<JobResponse> getMyPostedJobs(String clientEmail) {
        return jobRepository.findByClientId(findUser(clientEmail, "Client").getId()).stream()
                .map(jobMapper::toResponse).toList();
    }

    @Override
    public List<JobResponse> getMyAcceptedJobs(String freelancerEmail) {
        return jobRepository.findByFreelancerId(findUser(freelancerEmail, "Freelancer").getId()).stream()
                .map(jobMapper::toResponse).toList();
    }

    private User findUser(String email, String label) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(label + " not found"));
    }
}
