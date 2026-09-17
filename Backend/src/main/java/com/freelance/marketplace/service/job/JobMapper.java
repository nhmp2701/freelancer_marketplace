package com.freelance.marketplace.service.job;

import com.freelance.marketplace.dto.response.JobResponse;
import com.freelance.marketplace.entity.Job;
import com.freelance.marketplace.entity.Skill;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {
  public JobResponse toResponse(Job job) {
    return JobResponse.builder()
        .id(job.getId())
        .clientId(job.getClient().getId())
        .clientName(job.getClient().getFullName())
        .freelancerId(job.getFreelancer() != null ? job.getFreelancer().getId() : null)
        .freelancerName(job.getFreelancer() != null ? job.getFreelancer().getFullName() : null)
        .title(job.getTitle())
        .description(job.getDescription())
        .budget(job.getBudget())
        .status(job.getStatus().name())
        .deadline(job.getDeadline())
        .submissionUrl(job.getSubmissionUrl())
        .submissionNote(job.getSubmissionNote())
        .revisionNote(job.getRevisionNote())
        .progress(job.getProgress())
        .progressNote(job.getProgressNote())
        .progressUpdatedAt(job.getProgressUpdatedAt())
        .skills(job.getSkills().stream().map(Skill::getName).collect(Collectors.toSet()))
        .createdAt(job.getCreatedAt())
        .updatedAt(job.getUpdatedAt())
        .build();
  }
}
