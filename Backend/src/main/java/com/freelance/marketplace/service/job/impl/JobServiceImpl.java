package com.freelance.marketplace.service.job.impl;

import com.freelance.marketplace.dto.request.CreateJobRequest;
import com.freelance.marketplace.dto.request.UpdateJobRequest;
import com.freelance.marketplace.dto.response.JobResponse;
import com.freelance.marketplace.entity.Job;
import com.freelance.marketplace.entity.Skill;
import com.freelance.marketplace.entity.User;
import com.freelance.marketplace.enums.JobStatus;
import com.freelance.marketplace.exception.ResourceNotFoundException;
import com.freelance.marketplace.repository.JobRepository;
import com.freelance.marketplace.repository.SkillRepository;
import com.freelance.marketplace.repository.UserRepository;
import com.freelance.marketplace.repository.specification.JobSpecification;
import com.freelance.marketplace.service.job.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    @Override
    @Transactional
    public JobResponse createJob(String clientEmail, CreateJobRequest request) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        Job job = Job.builder()
                .client(client)
                .title(request.getTitle())
                .description(request.getDescription())
                .budget(request.getBudget())
                .deadline(request.getDeadline())
                .status(JobStatus.OPEN)
                .skills(resolveSkills(request.getSkills()))
                .build();

        Job saved = jobRepository.save(job);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));
        return mapToResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponse> searchJobs(JobStatus status, BigDecimal minBudget, BigDecimal maxBudget, String skill, String keyword) {
        Specification<Job> spec = Specification.where(JobSpecification.hasStatus(status))
                .and(JobSpecification.hasMinBudget(minBudget))
                .and(JobSpecification.hasMaxBudget(maxBudget))
                .and(JobSpecification.hasSkill(skill))
                .and(JobSpecification.hasTitleContaining(keyword));

        return jobRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponse> getMyPostedJobs(String clientEmail) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        return jobRepository.findByClientId(client.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponse> getMyAcceptedJobs(String freelancerEmail) {
        User freelancer = userRepository.findByEmail(freelancerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer not found"));
        return jobRepository.findByFreelancerId(freelancer.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobResponse updateJob(String clientEmail, Long jobId, UpdateJobRequest request) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // Chỉ chủ sở hữu mới được sửa
        if (!job.getClient().getId().equals(client.getId())) {
            throw new AccessDeniedException("You are not the owner of this job");
        }

        // Chỉ sửa được khi OPEN
        if (job.getStatus() != JobStatus.OPEN) {
            throw new IllegalStateException("Only OPEN jobs can be updated");
        }

        if (request.getTitle() != null) job.setTitle(request.getTitle());
        if (request.getDescription() != null) job.setDescription(request.getDescription());
        if (request.getBudget() != null) job.setBudget(request.getBudget());
        if (request.getDeadline() != null) job.setDeadline(request.getDeadline());
        if (request.getSkills() != null) job.setSkills(resolveSkills(request.getSkills()));

        return mapToResponse(jobRepository.save(job));
    }

    @Override
    @Transactional
    public void cancelJob(String clientEmail, Long jobId) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getClient().getId().equals(client.getId())) {
            throw new AccessDeniedException("You are not the owner of this job");
        }

        if (job.getStatus() != JobStatus.OPEN) {
            throw new IllegalStateException("Only OPEN jobs can be cancelled");
        }

        job.setStatus(JobStatus.CANCELLED);
        jobRepository.save(job);
    }

    private Set<Skill> resolveSkills(Set<String> skillNames) {
        if (skillNames == null || skillNames.isEmpty()) return new HashSet<>();
        Set<Skill> skills = new HashSet<>();
        for (String name : skillNames) {
            if (name == null || name.isBlank()) continue;
            String normalizedName = name.trim();
            Skill skill = skillRepository.findByNameIgnoreCase(normalizedName)
                    .orElseGet(() -> skillRepository.save(Skill.builder().name(normalizedName).build()));
            skills.add(skill);
        }
        return skills;
    }

    private JobResponse mapToResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .clientId(job.getClient().getId())
                .clientName(job.getClient().getFullName())
                .freelancerId(job.getFreelancer() != null ? job.getFreelancer().getId() : null)
                .freelancerName(job.getFreelancer() != null ? job.getFreelancer().getFullName() : null)
                .title(job.getTitle())
                .description(job.getDescription())
                .budget(job.getBudget())
                .status(job.getStatus().name())
                .deadline(job.getDeadline())
                .skills(job.getSkills().stream().map(Skill::getName).collect(Collectors.toSet()))
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}
