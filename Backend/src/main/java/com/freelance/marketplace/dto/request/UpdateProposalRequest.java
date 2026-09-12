package com.freelance.marketplace.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProposalRequest {
    @Size(min = 1, max = 5000)
    private String coverLetter;

    @DecimalMin("0.01")
    private BigDecimal bidAmount;

    @Min(1)
    private Integer deliveryDays;
}
