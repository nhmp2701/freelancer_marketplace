package com.freelance.marketplace.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.freelance.marketplace.repository.JobRepository;
import com.freelance.marketplace.repository.UserRepository;
import com.freelance.marketplace.service.job.JobMapper;
import com.freelance.marketplace.service.job.impl.JobQueryServiceImpl;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class JobQueryServiceImplTest {

  private final JobQueryServiceImpl service =
      new JobQueryServiceImpl(
          mock(JobRepository.class), mock(UserRepository.class), mock(JobMapper.class));

  @Test
  void rejectsInvertedBudgetRange() {
    assertThatThrownBy(
            () ->
                service.searchJobs(
                    null, BigDecimal.TEN, BigDecimal.ONE, null, null, 0, 20, "createdAt", "desc"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsUnknownSortField() {
    assertThatThrownBy(
            () ->
                service.searchJobs(
                    null, null, null, null, null, 0, 20, "client.passwordHash", "desc"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
