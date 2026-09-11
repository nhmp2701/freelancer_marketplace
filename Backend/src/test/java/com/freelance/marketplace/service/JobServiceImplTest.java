package com.freelance.marketplace.service;

import com.freelance.marketplace.dto.request.UpdateJobRequest;
import com.freelance.marketplace.entity.Job;
import com.freelance.marketplace.entity.User;
import com.freelance.marketplace.enums.JobStatus;
import com.freelance.marketplace.repository.JobRepository;
import com.freelance.marketplace.repository.SkillRepository;
import com.freelance.marketplace.repository.UserRepository;
import com.freelance.marketplace.service.job.impl.JobServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JobServiceImplTest {

    private final JobRepository jobRepository = mock(JobRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final SkillRepository skillRepository = mock(SkillRepository.class);
    private final JobServiceImpl service = new JobServiceImpl(jobRepository, userRepository, skillRepository);

    @Test
    void rejectsUpdateByNonOwner() {
        User owner = User.builder().id(1L).build();
        User otherUser = User.builder().id(2L).email("other@example.com").build();
        Job job = Job.builder().id(10L).client(owner).status(JobStatus.OPEN).build();
        when(userRepository.findByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.updateJob(otherUser.getEmail(), job.getId(), new UpdateJobRequest()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejectsUpdateAfterJobLeavesOpenState() {
        User owner = User.builder().id(1L).email("owner@example.com").build();
        Job job = Job.builder().id(10L).client(owner).status(JobStatus.IN_PROGRESS).build();
        when(userRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.updateJob(owner.getEmail(), job.getId(), new UpdateJobRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only OPEN jobs can be updated");
    }
}
