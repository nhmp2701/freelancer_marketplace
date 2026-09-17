package com.freelance.marketplace.enums;

public enum JobStatus {
  OPEN, // Đang mở, chờ ứng viên
  IN_PROGRESS, // Đã chọn freelancer, đang làm
  SUBMITTED, // Freelancer đã nộp sản phẩm, chờ nghiệm thu
  COMPLETED, // Đã hoàn thành (terminal)
  CANCELLED, // Đã hủy (terminal)
  DISPUTED // Đang tranh chấp
}
