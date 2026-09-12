package com.freelance.marketplace.service;

import com.freelance.marketplace.dto.request.CreateProposalRequest;
import com.freelance.marketplace.entity.Job;
import com.freelance.marketplace.entity.Proposal;
import com.freelance.marketplace.entity.User;
import com.freelance.marketplace.enums.JobStatus;
import com.freelance.marketplace.enums.ProposalStatus;
import com.freelance.marketplace.repository.JobRepository;
import com.freelance.marketplace.repository.JobStatusHistoryRepository;
import com.freelance.marketplace.repository.ProposalRepository;
import com.freelance.marketplace.repository.UserRepository;
import com.freelance.marketplace.service.proposal.impl.ProposalServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProposalServiceImplTest {
    private final ProposalRepository proposalRepository = mock(ProposalRepository.class);
    private final JobRepository jobRepository = mock(JobRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final JobStatusHistoryRepository historyRepository = mock(JobStatusHistoryRepository.class);
    private final ProposalServiceImpl service = new ProposalServiceImpl(
            proposalRepository, jobRepository, userRepository, historyRepository);

    @Test
    void createsPendingProposalForOpenJob() {
        User client = User.builder().id(1L).build();
        User freelancer = User.builder().id(2L).email("freelancer@example.com").fullName("Freelancer").build();
        Job job = Job.builder().id(10L).client(client).title("Job").status(JobStatus.OPEN).build();
        when(userRepository.findByEmail(freelancer.getEmail())).thenReturn(Optional.of(freelancer));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(proposalRepository.save(any())).thenAnswer(invocation -> {
            Proposal proposal = invocation.getArgument(0);
            proposal.setId(100L);
            return proposal;
        });

        var response = service.create(freelancer.getEmail(), job.getId(), request());

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        verify(proposalRepository).save(any(Proposal.class));
    }

    @Test
    void rejectsProposalFromJobOwner() {
        User owner = User.builder().id(1L).email("owner@example.com").build();
        Job job = Job.builder().id(10L).client(owner).status(JobStatus.OPEN).build();
        when(userRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.create(owner.getEmail(), job.getId(), request()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejectsDuplicateActiveProposal() {
        User freelancer = User.builder().id(2L).email("freelancer@example.com").build();
        Job job = Job.builder().id(10L).client(User.builder().id(1L).build()).status(JobStatus.OPEN).build();
        when(userRepository.findByEmail(freelancer.getEmail())).thenReturn(Optional.of(freelancer));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(proposalRepository.existsByJobIdAndFreelancerIdAndStatus(
                job.getId(), freelancer.getId(), ProposalStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> service.create(freelancer.getEmail(), job.getId(), request()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsWithdrawalByAnotherUser() {
        User owner = User.builder().id(2L).email("owner@example.com").build();
        User other = User.builder().id(3L).email("other@example.com").build();
        Proposal proposal = Proposal.builder().id(100L).freelancer(owner).status(ProposalStatus.PENDING).build();
        when(userRepository.findByEmail(other.getEmail())).thenReturn(Optional.of(other));
        when(proposalRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));

        assertThatThrownBy(() -> service.withdraw(other.getEmail(), proposal.getId()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ownerAcceptsProposalAndStartsJob() {
        User client = User.builder().id(1L).email("client@example.com").build();
        User freelancer = User.builder().id(2L).fullName("Freelancer").build();
        Job job = Job.builder().id(10L).client(client).title("Job").status(JobStatus.OPEN).build();
        Proposal proposal = Proposal.builder().id(100L).job(job).freelancer(freelancer)
                .status(ProposalStatus.PENDING).build();
        when(userRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(proposalRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));
        when(jobRepository.findLockedById(job.getId())).thenReturn(Optional.of(job));

        var response = service.accept(client.getEmail(), proposal.getId());

        assertThat(response.getStatus()).isEqualTo("ACCEPTED");
        assertThat(job.getStatus()).isEqualTo(JobStatus.IN_PROGRESS);
        assertThat(job.getFreelancer()).isSameAs(freelancer);
        verify(historyRepository).save(any());
    }

    private CreateProposalRequest request() {
        CreateProposalRequest request = new CreateProposalRequest();
        request.setCoverLetter("I can deliver this job");
        request.setBidAmount(new BigDecimal("100.00"));
        request.setDeliveryDays(7);
        return request;
    }
}
