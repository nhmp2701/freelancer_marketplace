package com.freelance.marketplace.service.job;

import com.freelance.marketplace.dto.request.CreateJobRequest;
import com.freelance.marketplace.dto.request.SubmitJobRequest;
import com.freelance.marketplace.dto.request.UpdateJobRequest;
import com.freelance.marketplace.dto.response.JobResponse;

public interface JobCommandService {
  JobResponse createJob(String clientEmail, CreateJobRequest request);

  JobResponse updateJob(String clientEmail, Long jobId, UpdateJobRequest request);

  void cancelJob(String clientEmail, Long jobId);

  JobResponse submit(String freelancerEmail, Long jobId, SubmitJobRequest request);

  JobResponse acceptSubmission(String clientEmail, Long jobId);

  JobResponse requestChanges(String clientEmail, Long jobId, String reason);

  JobResponse updateProgress(String freelancerEmail, Long jobId, int progress, String note);
}
