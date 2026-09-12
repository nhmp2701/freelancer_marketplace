package com.freelance.marketplace.service.proposal.impl;

import com.freelance.marketplace.dto.request.CreateProposalRequest;
import com.freelance.marketplace.dto.request.UpdateProposalRequest;
import com.freelance.marketplace.dto.response.PageResponse;
import com.freelance.marketplace.dto.response.ProposalResponse;
import com.freelance.marketplace.entity.Job;
import com.freelance.marketplace.entity.JobStatusHistory;
import com.freelance.marketplace.entity.Proposal;
import com.freelance.marketplace.entity.User;
import com.freelance.marketplace.enums.JobStatus;
import com.freelance.marketplace.enums.ProposalStatus;
import com.freelance.marketplace.exception.ResourceNotFoundException;
import com.freelance.marketplace.repository.JobRepository;
import com.freelance.marketplace.repository.JobStatusHistoryRepository;
import com.freelance.marketplace.repository.ProposalRepository;
import com.freelance.marketplace.repository.UserRepository;
import com.freelance.marketplace.service.proposal.ProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProposalServiceImpl implements ProposalService {
    private final ProposalRepository proposalRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobStatusHistoryRepository jobStatusHistoryRepository;

    @Override
    @Transactional
    public ProposalResponse create(String email, Long jobId, CreateProposalRequest request) {
        User freelancer = findUser(email);
        Job job = findJob(jobId);
        if (job.getStatus() != JobStatus.OPEN) {
            throw new IllegalStateException("Only OPEN jobs accept proposals");
        }
        if (job.getClient().getId().equals(freelancer.getId())) {
            throw new AccessDeniedException("Job owner cannot submit a proposal");
        }
        if (proposalRepository.existsByJobIdAndFreelancerIdAndStatus(
                jobId, freelancer.getId(), ProposalStatus.PENDING)) {
            throw new IllegalStateException("An active proposal already exists for this job");
        }
        Proposal proposal = Proposal.builder()
                .job(job)
                .freelancer(freelancer)
                .coverLetter(request.getCoverLetter().trim())
                .bidAmount(request.getBidAmount())
                .deliveryDays(request.getDeliveryDays())
                .status(ProposalStatus.PENDING)
                .build();
        return toResponse(proposalRepository.save(proposal));
    }

    @Override
    @Transactional
    public ProposalResponse update(String email, Long proposalId, UpdateProposalRequest request) {
        Proposal proposal = findOwnedPendingProposal(email, proposalId);
        if (proposal.getJob().getStatus() != JobStatus.OPEN) {
            throw new IllegalStateException("Proposal cannot be updated after the job closes");
        }
        if (request.getCoverLetter() != null && request.getCoverLetter().isBlank()) {
            throw new IllegalArgumentException("Cover letter must not be blank");
        }
        if (request.getCoverLetter() != null) proposal.setCoverLetter(request.getCoverLetter().trim());
        if (request.getBidAmount() != null) proposal.setBidAmount(request.getBidAmount());
        if (request.getDeliveryDays() != null) proposal.setDeliveryDays(request.getDeliveryDays());
        return toResponse(proposalRepository.save(proposal));
    }

    @Override
    @Transactional
    public void withdraw(String email, Long proposalId) {
        Proposal proposal = findOwnedPendingProposal(email, proposalId);
        if (proposal.getJob().getStatus() != JobStatus.OPEN) {
            throw new IllegalStateException("Proposal cannot be withdrawn after the job closes");
        }
        proposal.setStatus(ProposalStatus.WITHDRAWN);
        proposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public ProposalResponse accept(String email, Long proposalId) {
        User client = findUser(email);
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found"));
        Job job = jobRepository.findLockedById(proposal.getJob().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (!job.getClient().getId().equals(client.getId())) {
            throw new AccessDeniedException("You are not the owner of this job");
        }
        if (job.getStatus() != JobStatus.OPEN || proposal.getStatus() != ProposalStatus.PENDING) {
            throw new IllegalStateException("Only a PENDING proposal on an OPEN job can be accepted");
        }

        job.setFreelancer(proposal.getFreelancer());
        job.setStatus(JobStatus.IN_PROGRESS);
        proposal.setStatus(ProposalStatus.ACCEPTED);
        jobRepository.save(job);
        proposalRepository.save(proposal);
        proposalRepository.rejectOtherPending(job.getId(), proposal.getId(),
                ProposalStatus.PENDING, ProposalStatus.REJECTED);
        jobStatusHistoryRepository.save(JobStatusHistory.builder()
                .job(job).actor(client).fromStatus(JobStatus.OPEN).toStatus(JobStatus.IN_PROGRESS)
                .note("Proposal " + proposal.getId() + " accepted").build());
        return toResponse(proposal);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProposalResponse> getMine(String email, int page, int size) {
        User user = findUser(email);
        return PageResponse.from(proposalRepository.findByFreelancerId(
                user.getId(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))), this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProposalResponse> getForOwnedJob(String email, Long jobId, int page, int size) {
        User user = findUser(email);
        Job job = findJob(jobId);
        if (!job.getClient().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not the owner of this job");
        }
        return PageResponse.from(proposalRepository.findByJobId(
                jobId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))), this::toResponse);
    }

    private Proposal findOwnedPendingProposal(String email, Long proposalId) {
        User user = findUser(email);
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found"));
        if (!proposal.getFreelancer().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not the owner of this proposal");
        }
        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new IllegalStateException("Only PENDING proposals can be changed");
        }
        return proposal;
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Job findJob(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    private ProposalResponse toResponse(Proposal proposal) {
        return ProposalResponse.builder()
                .id(proposal.getId())
                .jobId(proposal.getJob().getId())
                .jobTitle(proposal.getJob().getTitle())
                .freelancerId(proposal.getFreelancer().getId())
                .freelancerName(proposal.getFreelancer().getFullName())
                .coverLetter(proposal.getCoverLetter())
                .bidAmount(proposal.getBidAmount())
                .deliveryDays(proposal.getDeliveryDays())
                .status(proposal.getStatus().name())
                .createdAt(proposal.getCreatedAt())
                .updatedAt(proposal.getUpdatedAt())
                .build();
    }
}
