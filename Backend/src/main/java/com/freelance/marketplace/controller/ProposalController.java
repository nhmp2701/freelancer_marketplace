package com.freelance.marketplace.controller;

import com.freelance.marketplace.dto.request.CreateProposalRequest;
import com.freelance.marketplace.dto.request.UpdateProposalRequest;
import com.freelance.marketplace.dto.response.PageResponse;
import com.freelance.marketplace.dto.response.ProposalResponse;
import com.freelance.marketplace.service.proposal.ProposalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ProposalController {
    private final ProposalService proposalService;

    @PostMapping("/jobs/{jobId}/proposals")
    public ResponseEntity<ProposalResponse> create(Authentication authentication, @PathVariable Long jobId,
                                                    @Valid @RequestBody CreateProposalRequest request) {
        return new ResponseEntity<>(proposalService.create(authentication.getName(), jobId, request), HttpStatus.CREATED);
    }

    @PutMapping("/proposals/{proposalId}")
    public ResponseEntity<ProposalResponse> update(Authentication authentication, @PathVariable Long proposalId,
                                                    @Valid @RequestBody UpdateProposalRequest request) {
        return ResponseEntity.ok(proposalService.update(authentication.getName(), proposalId, request));
    }

    @DeleteMapping("/proposals/{proposalId}")
    public ResponseEntity<Void> withdraw(Authentication authentication, @PathVariable Long proposalId) {
        proposalService.withdraw(authentication.getName(), proposalId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/proposals/{proposalId}/accept")
    public ResponseEntity<ProposalResponse> accept(Authentication authentication, @PathVariable Long proposalId) {
        return ResponseEntity.ok(proposalService.accept(authentication.getName(), proposalId));
    }

    @GetMapping("/proposals/me")
    public ResponseEntity<PageResponse<ProposalResponse>> getMine(
            Authentication authentication, @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(proposalService.getMine(authentication.getName(), page, size));
    }

    @GetMapping("/jobs/{jobId}/proposals")
    public ResponseEntity<PageResponse<ProposalResponse>> getForJob(
            Authentication authentication, @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(proposalService.getForOwnedJob(authentication.getName(), jobId, page, size));
    }
}
