package com.freelance.marketplace.repository;

import com.freelance.marketplace.entity.Job;
import com.freelance.marketplace.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    List<Job> findByClientId(Long clientId);
    List<Job> findByFreelancerId(Long freelancerId);
    List<Job> findByStatus(JobStatus status);
}