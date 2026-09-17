package com.freelance.marketplace.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.freelance.marketplace.dto.request.SubmitJobRequest;
import com.freelance.marketplace.dto.request.UpdateJobRequest;
import com.freelance.marketplace.entity.Job;
import com.freelance.marketplace.entity.Proposal;
import com.freelance.marketplace.entity.User;
import com.freelance.marketplace.enums.JobStatus;
import com.freelance.marketplace.enums.ProposalStatus;
import com.freelance.marketplace.repository.JobRepository;
import com.freelance.marketplace.repository.JobStatusHistoryRepository;
import com.freelance.marketplace.repository.ProposalRepository;
import com.freelance.marketplace.repository.UserRepository;
import com.freelance.marketplace.service.job.JobMapper;
import com.freelance.marketplace.service.job.impl.JobCommandServiceImpl;
import com.freelance.marketplace.service.skill.SkillResolver;
import com.freelance.marketplace.service.wallet.WalletService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class JobCommandServiceImplTest {

  private final JobRepository jobRepository = mock(JobRepository.class);
  private final UserRepository userRepository = mock(UserRepository.class);
  private final SkillResolver skillResolver = mock(SkillResolver.class);
  private final JobMapper jobMapper = mock(JobMapper.class);
  private final JobStatusHistoryRepository historyRepository =
      mock(JobStatusHistoryRepository.class);
  private final ProposalRepository proposalRepository = mock(ProposalRepository.class);
  private final WalletService walletService = mock(WalletService.class);
  private final JobCommandServiceImpl service =
      new JobCommandServiceImpl(
          jobRepository,
          userRepository,
          skillResolver,
          jobMapper,
          historyRepository,
          proposalRepository,
          walletService);

  @Test
  void rejectsUpdateByNonOwner() {
    User owner = User.builder().id(1L).build();
    User otherUser = User.builder().id(2L).email("other@example.com").build();
    Job job = Job.builder().id(10L).client(owner).status(JobStatus.OPEN).build();
    when(userRepository.findByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));
    when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

    assertThatThrownBy(
            () -> service.updateJob(otherUser.getEmail(), job.getId(), new UpdateJobRequest()))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void rejectsUpdateAfterJobLeavesOpenState() {
    User owner = User.builder().id(1L).email("owner@example.com").build();
    Job job = Job.builder().id(10L).client(owner).status(JobStatus.IN_PROGRESS).build();
    when(userRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
    when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

    assertThatThrownBy(
            () -> service.updateJob(owner.getEmail(), job.getId(), new UpdateJobRequest()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Only OPEN jobs can be updated");
  }

  @Test
  void assignedFreelancerCanSubmitWork() {
    User client = User.builder().id(1L).build();
    User freelancer = User.builder().id(2L).email("freelancer@example.com").build();
    Job job =
        Job.builder()
            .id(10L)
            .client(client)
            .freelancer(freelancer)
            .status(JobStatus.IN_PROGRESS)
            .build();
    SubmitJobRequest request = new SubmitJobRequest();
    request.setSubmissionUrl("https://example.com/delivery");
    request.setSubmissionNote("Ready for review");
    when(userRepository.findByEmail(freelancer.getEmail())).thenReturn(Optional.of(freelancer));
    when(jobRepository.findLockedById(job.getId())).thenReturn(Optional.of(job));

    service.submit(freelancer.getEmail(), job.getId(), request);

    org.assertj.core.api.Assertions.assertThat(job.getStatus()).isEqualTo(JobStatus.SUBMITTED);
    org.assertj.core.api.Assertions.assertThat(job.getSubmissionNote())
        .isEqualTo("Ready for review");
    verify(historyRepository).save(any());
  }

  @Test
  void assignedFreelancerCanUpdateProgress() {
    User client = User.builder().id(1L).build();
    User freelancer = User.builder().id(2L).email("freelancer@example.com").build();
    Job job =
        Job.builder()
            .id(10L)
            .client(client)
            .freelancer(freelancer)
            .status(JobStatus.IN_PROGRESS)
            .build();
    when(userRepository.findByEmail(freelancer.getEmail())).thenReturn(Optional.of(freelancer));
    when(jobRepository.findLockedById(job.getId())).thenReturn(Optional.of(job));

    service.updateProgress(freelancer.getEmail(), job.getId(), 60, "Đã hoàn thành giao diện chính");

    org.assertj.core.api.Assertions.assertThat(job.getProgress()).isEqualTo(60);
    org.assertj.core.api.Assertions.assertThat(job.getProgressNote())
        .isEqualTo("Đã hoàn thành giao diện chính");
    verify(jobRepository).save(job);
  }

  @Test
  void clientCannotUpdateFreelancerProgress() {
    User client = User.builder().id(1L).email("client@example.com").build();
    User freelancer = User.builder().id(2L).build();
    Job job =
        Job.builder()
            .id(10L)
            .client(client)
            .freelancer(freelancer)
            .status(JobStatus.IN_PROGRESS)
            .build();
    when(userRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
    when(jobRepository.findLockedById(job.getId())).thenReturn(Optional.of(job));

    assertThatThrownBy(() -> service.updateProgress(client.getEmail(), job.getId(), 50, "test"))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void acceptingSubmissionReleasesAcceptedBid() {
    User client = User.builder().id(1L).email("client@example.com").build();
    User freelancer = User.builder().id(2L).build();
    Job job =
        Job.builder()
            .id(10L)
            .client(client)
            .freelancer(freelancer)
            .status(JobStatus.SUBMITTED)
            .build();
    Proposal proposal =
        Proposal.builder()
            .job(job)
            .freelancer(freelancer)
            .status(ProposalStatus.ACCEPTED)
            .bidAmount(new BigDecimal("250000"))
            .build();
    when(userRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
    when(jobRepository.findLockedById(job.getId())).thenReturn(Optional.of(job));
    when(proposalRepository.findByJobIdAndStatus(job.getId(), ProposalStatus.ACCEPTED))
        .thenReturn(Optional.of(proposal));

    service.acceptSubmission(client.getEmail(), job.getId());

    verify(walletService).release(client, freelancer, job, proposal.getBidAmount());
    org.assertj.core.api.Assertions.assertThat(job.getStatus()).isEqualTo(JobStatus.COMPLETED);
  }
}
