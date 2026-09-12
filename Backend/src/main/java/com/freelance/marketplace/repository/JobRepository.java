package com.freelance.marketplace.repository;

import com.freelance.marketplace.entity.Job;
import com.freelance.marketplace.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    List<Job> findByClientId(Long clientId);
    List<Job> findByFreelancerId(Long freelancerId);
    List<Job> findByStatus(JobStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Job> findLockedById(Long id);
}
