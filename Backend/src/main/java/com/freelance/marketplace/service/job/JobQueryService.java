package com.freelance.marketplace.service.job;

import com.freelance.marketplace.dto.response.JobResponse;
import com.freelance.marketplace.enums.JobStatus;

import java.math.BigDecimal;
import java.util.List;

public interface JobQueryService {
    JobResponse getJobById(Long jobId);
    List<JobResponse> searchJobs(JobStatus status, BigDecimal minBudget, BigDecimal maxBudget, String skill, String keyword);
    List<JobResponse> getMyPostedJobs(String clientEmail);
    List<JobResponse> getMyAcceptedJobs(String freelancerEmail);
}
