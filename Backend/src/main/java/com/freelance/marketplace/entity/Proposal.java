package com.freelance.marketplace.entity;

import com.freelance.marketplace.enums.ProposalStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "proposals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proposal {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "job_id", nullable = false)
  private Job job;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "freelancer_id", nullable = false)
  private User freelancer;

  @Column(name = "cover_letter", nullable = false, columnDefinition = "TEXT")
  private String coverLetter;

  @Column(name = "bid_amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal bidAmount;

  @Column(name = "delivery_days", nullable = false)
  private Integer deliveryDays;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ProposalStatus status;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
