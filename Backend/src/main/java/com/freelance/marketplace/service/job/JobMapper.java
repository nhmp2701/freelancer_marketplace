package com.freelance.marketplace.service.job;

import com.freelance.marketplace.dto.response.JobResponse;
import com.freelance.marketplace.entity.Job;
import com.freelance.marketplace.entity.Skill;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

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
                .skills(job.getSkills().stream().map(Skill::getName).collect(Collectors.toSet()))
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}
