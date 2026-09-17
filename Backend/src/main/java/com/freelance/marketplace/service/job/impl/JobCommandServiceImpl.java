package com.freelance.marketplace.service.job.impl;

import com.freelance.marketplace.dto.request.CreateJobRequest;
import com.freelance.marketplace.dto.request.SubmitJobRequest;
import com.freelance.marketplace.dto.request.UpdateJobRequest;
import com.freelance.marketplace.dto.response.JobResponse;
import com.freelance.marketplace.entity.Job;
import com.freelance.marketplace.entity.JobStatusHistory;
import com.freelance.marketplace.entity.User;
import com.freelance.marketplace.enums.JobStatus;
import com.freelance.marketplace.enums.ProposalStatus;
import com.freelance.marketplace.exception.ResourceNotFoundException;
import com.freelance.marketplace.repository.JobRepository;
import com.freelance.marketplace.repository.JobStatusHistoryRepository;
import com.freelance.marketplace.repository.ProposalRepository;
import com.freelance.marketplace.repository.UserRepository;
import com.freelance.marketplace.service.job.JobCommandService;
import com.freelance.marketplace.service.job.JobMapper;
import com.freelance.marketplace.service.skill.SkillResolver;
import com.freelance.marketplace.service.wallet.WalletService;
import java.time.LocalDateTime;
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
  private final JobStatusHistoryRepository historyRepository;
  private final ProposalRepository proposalRepository;
  private final WalletService walletService;

  @Override
  @Transactional
  public JobResponse createJob(String clientEmail, CreateJobRequest request) {
    User client = findUser(clientEmail, "Client");
    Job job =
        Job.builder()
            .client(client)
            .title(request.getTitle())
            .description(request.getDescription())
            .budget(request.getBudget())
            .deadline(request.getDeadline())
            .status(JobStatus.OPEN)
            .skills(skillResolver.resolve(request.getSkills()))
            .build();
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

  @Override
  @Transactional
  /** Người thực hiện bàn giao sản phẩm và chuyển dự án sang trạng thái chờ nghiệm thu. */
  public JobResponse submit(String freelancerEmail, Long jobId, SubmitJobRequest request) {
    User freelancer = findUser(freelancerEmail, "Freelancer");
    Job job = findLockedJob(jobId);
    if (job.getFreelancer() == null || !job.getFreelancer().getId().equals(freelancer.getId())) {
      throw new AccessDeniedException("Only the assigned freelancer can submit this job");
    }
    requireStatus(job, JobStatus.IN_PROGRESS);
    job.setSubmissionUrl(request.getSubmissionUrl());
    job.setSubmissionNote(request.getSubmissionNote());
    job.setRevisionNote(null);
    job.setProgress(100);
    job.setProgressNote("Đã gửi bàn giao");
    job.setProgressUpdatedAt(LocalDateTime.now());
    transition(job, freelancer, JobStatus.SUBMITTED, "Work submitted for review");
    return jobMapper.toResponse(jobRepository.save(job));
  }

  @Override
  @Transactional
  /** Nghiệm thu và giải ngân nằm trong cùng transaction để tiền và trạng thái không lệch nhau. */
  public JobResponse acceptSubmission(String clientEmail, Long jobId) {
    User client = findUser(clientEmail, "Client");
    Job job = findLockedJob(jobId);
    requireOwner(job, client);
    requireStatus(job, JobStatus.SUBMITTED);
    var proposal =
        proposalRepository
            .findByJobIdAndStatus(jobId, ProposalStatus.ACCEPTED)
            .orElseThrow(() -> new IllegalStateException("Accepted proposal not found"));
    walletService.release(client, job.getFreelancer(), job, proposal.getBidAmount());
    transition(job, client, JobStatus.COMPLETED, "Submission accepted");
    return jobMapper.toResponse(jobRepository.save(job));
  }

  @Override
  @Transactional
  /** Khách hàng yêu cầu sửa và đưa dự án trở lại trạng thái đang thực hiện. */
  public JobResponse requestChanges(String clientEmail, Long jobId, String reason) {
    User client = findUser(clientEmail, "Client");
    Job job = findLockedJob(jobId);
    requireOwner(job, client);
    requireStatus(job, JobStatus.SUBMITTED);
    job.setRevisionNote(reason);
    transition(job, client, JobStatus.IN_PROGRESS, reason);
    return jobMapper.toResponse(jobRepository.save(job));
  }

  @Override
  @Transactional
  /** Chỉ người được giao dự án mới được cập nhật phần trăm và ghi chú tiến độ. */
  public JobResponse updateProgress(String freelancerEmail, Long jobId, int progress, String note) {
    User freelancer = findUser(freelancerEmail, "Freelancer");
    Job job = findLockedJob(jobId);
    if (job.getFreelancer() == null || !job.getFreelancer().getId().equals(freelancer.getId())) {
      throw new AccessDeniedException("Only the assigned freelancer can update progress");
    }
    requireStatus(job, JobStatus.IN_PROGRESS);
    job.setProgress(progress);
    job.setProgressNote(note.trim());
    job.setProgressUpdatedAt(LocalDateTime.now());
    return jobMapper.toResponse(jobRepository.save(job));
  }

  private Job findLockedJob(Long jobId) {
    return jobRepository
        .findLockedById(jobId)
        .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
  }

  private void requireOwner(Job job, User client) {
    if (!job.getClient().getId().equals(client.getId())) {
      throw new AccessDeniedException("Only the job owner can review submissions");
    }
  }

  private void requireStatus(Job job, JobStatus expected) {
    if (job.getStatus() != expected) {
      throw new IllegalStateException("Job must be " + expected);
    }
  }

  private void transition(Job job, User actor, JobStatus to, String note) {
    JobStatus from = job.getStatus();
    job.setStatus(to);
    historyRepository.save(
        JobStatusHistory.builder()
            .job(job)
            .actor(actor)
            .fromStatus(from)
            .toStatus(to)
            .note(note)
            .build());
  }

  private Job findOwnedOpenJob(String email, Long jobId, String action) {
    User client = findUser(email, "Client");
    Job job =
        jobRepository
            .findById(jobId)
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
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException(label + " not found"));
  }
}
