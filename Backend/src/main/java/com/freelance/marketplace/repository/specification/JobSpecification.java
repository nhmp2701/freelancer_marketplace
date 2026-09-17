package com.freelance.marketplace.repository.specification;

import com.freelance.marketplace.entity.Job;
import com.freelance.marketplace.entity.Skill;
import com.freelance.marketplace.enums.JobStatus;
import jakarta.persistence.criteria.Join;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;

public class JobSpecification {

  public static Specification<Job> hasStatus(JobStatus status) {
    return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
  }

  public static Specification<Job> hasMinBudget(BigDecimal minBudget) {
    return (root, query, cb) ->
        minBudget == null ? null : cb.greaterThanOrEqualTo(root.get("budget"), minBudget);
  }

  public static Specification<Job> hasMaxBudget(BigDecimal maxBudget) {
    return (root, query, cb) ->
        maxBudget == null ? null : cb.lessThanOrEqualTo(root.get("budget"), maxBudget);
  }

  public static Specification<Job> hasSkill(String skillName) {
    return (root, query, cb) -> {
      if (skillName == null || skillName.isEmpty()) return null;
      Join<Job, Skill> skills = root.join("skills");
      return cb.equal(skills.get("name"), skillName);
    };
  }

  public static Specification<Job> hasTitleContaining(String keyword) {
    return (root, query, cb) -> {
      if (keyword == null || keyword.isEmpty()) return null;
      return cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
    };
  }
}
