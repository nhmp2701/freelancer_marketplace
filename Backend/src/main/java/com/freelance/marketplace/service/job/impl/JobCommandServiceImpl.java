package com.freelance.marketplace.service.job.impl;

import com.freelance.marketplace.dto.request.CreateJobRequest;
import com.freelance.marketplace.dto.request.UpdateJobRequest;
import com.freelance.marketplace.dto.response.JobResponse;
import com.freelance.marketplace.entity.Job;
import com.freelance.marketplace.entity.User;
import com.freelance.marketplace.enums.JobStatus;
import com.freelance.marketplace.exception.ResourceNotFoundException;
import com.freelance.marketplace.repository.JobRepository;
import com.freelance.marketplace.repository.UserRepository;
import com.freelance.marketplace.service.job.JobCommandService;
import com.freelance.marketplace.service.job.JobMapper;
import com.freelance.marketplace.service.skill.SkillResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobCommandServiceImpl implements JobCommandService {
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final SkillResolver skillResolver;
    private final JobMapper jobMapper;

    @Override
    @Transactional
    public JobResponse createJob(String clientEmail, CreateJobRequest request) {
        User client = findUser(clientEmail, "Client");
        Job job = Job.builder()
                .client(client).title(request.getTitle()).description(request.getDescription())
                .budget(request.getBudget()).deadline(request.getDeadline()).status(JobStatus.OPEN)
                .skills(skillResolver.resolve(request.getSkills())).build();
        return jobMapper.toResponse(jobRepository.save(job));
    }

    @Override
    @Transactional
    public JobResponse updateJob(String clientEmail, Long jobId, UpdateJobRequest request) {
        Job job = findOwnedOpenJob(clientEmail, jobId, "updated");
        if (request.getTitle() != null) job.setTitle(request.getTitle());
        if (request.getDescription() != null) job.setDescription(request.getDescription());
        if (request.getBudget() != null) job.setBudget(request.getBudget());
        if (request.getDeadline() != null) job.setDeadline(request.getDeadline());
        if (request.getSkills() != null) job.setSkills(skillResolver.resolve(request.getSkills()));
        return jobMapper.toResponse(jobRepository.save(job));
    }

    @Override
    @Transactional
    public void cancelJob(String clientEmail, Long jobId) {
        Job job = findOwnedOpenJob(clientEmail, jobId, "cancelled");
        job.setStatus(JobStatus.CANCELLED);
        jobRepository.save(job);
    }

    private Job findOwnedOpenJob(String email, Long jobId, String action) {
        User client = findUser(email, "Client");
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (!job.getClient().getId().equals(client.getId())) {
            throw new AccessDeniedException("You are not the owner of this job");
        }
        if (job.getStatus() != JobStatus.OPEN) {
            throw new IllegalStateException("Only OPEN jobs can be " + action);
        }
        return job;
    }

    private User findUser(String email, String label) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(label + " not found"));
    }
}
