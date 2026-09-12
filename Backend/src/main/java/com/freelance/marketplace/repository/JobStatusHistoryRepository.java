package com.freelance.marketplace.repository;

import com.freelance.marketplace.entity.JobStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobStatusHistoryRepository extends JpaRepository<JobStatusHistory, Long> {
}
