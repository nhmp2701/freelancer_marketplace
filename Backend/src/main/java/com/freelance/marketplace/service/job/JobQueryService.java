package com.freelance.marketplace.service.job;

import com.freelance.marketplace.dto.response.JobResponse;
import com.freelance.marketplace.dto.response.PageResponse;
import com.freelance.marketplace.enums.JobStatus;

import java.math.BigDecimal;
import java.util.List;

public interface JobQueryService {
    JobResponse getJobById(Long jobId);
    PageResponse<JobResponse> searchJobs(JobStatus status, BigDecimal minBudget, BigDecimal maxBudget,
                                         String skill, String keyword, int page, int size,
                                         String sortBy, String sortDirection);
    List<JobResponse> getMyPostedJobs(String clientEmail);
    List<JobResponse> getMyAcceptedJobs(String freelancerEmail);
}
