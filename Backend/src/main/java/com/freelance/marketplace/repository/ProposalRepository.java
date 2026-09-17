package com.freelance.marketplace.repository;

import com.freelance.marketplace.entity.Proposal;
import com.freelance.marketplace.enums.ProposalStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {
  boolean existsByJobIdAndFreelancerIdAndStatus(
      Long jobId, Long freelancerId, ProposalStatus status);

  Page<Proposal> findByFreelancerId(Long freelancerId, Pageable pageable);

  Page<Proposal> findByJobId(Long jobId, Pageable pageable);

  Optional<Proposal> findByJobIdAndStatus(Long jobId, ProposalStatus status);

  @Modifying
  @Query(
      "update Proposal p set p.status = :rejected where p.job.id = :jobId and p.id <> :acceptedId"
          + " and p.status = :pending")
  int rejectOtherPending(
      @Param("jobId") Long jobId,
      @Param("acceptedId") Long acceptedId,
      @Param("pending") ProposalStatus pending,
      @Param("rejected") ProposalStatus rejected);
}
