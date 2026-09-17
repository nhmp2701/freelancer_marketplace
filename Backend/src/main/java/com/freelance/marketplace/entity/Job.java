package com.freelance.marketplace.entity;

import com.freelance.marketplace.enums.JobStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_id", nullable = false)
  private User client;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "freelancer_id")
  private User freelancer;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal budget;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private JobStatus status;

  @Column private LocalDateTime deadline;

  @Column(name = "submission_url", length = 1000)
  private String submissionUrl;

  @Column(name = "submission_note", columnDefinition = "TEXT")
  private String submissionNote;

  @Column(name = "revision_note", length = 500)
  private String revisionNote;

  @Builder.Default
  @Column(nullable = false)
  private Integer progress = 0;

  @Column(name = "progress_note", length = 500)
  private String progressNote;

  @Column(name = "progress_updated_at")
  private LocalDateTime progressUpdatedAt;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @ManyToMany
  @JoinTable(
      name = "job_skills",
      joinColumns = @JoinColumn(name = "job_id"),
      inverseJoinColumns = @JoinColumn(name = "skill_id"))
  @Builder.Default
  private Set<Skill> skills = new HashSet<>();
}
