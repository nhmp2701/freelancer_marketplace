package com.freelance.marketplace.service.proposal;

import com.freelance.marketplace.dto.request.CreateProposalRequest;
import com.freelance.marketplace.dto.request.UpdateProposalRequest;
import com.freelance.marketplace.dto.response.PageResponse;
import com.freelance.marketplace.dto.response.ProposalResponse;

public interface ProposalService {
    ProposalResponse create(String email, Long jobId, CreateProposalRequest request);
    ProposalResponse update(String email, Long proposalId, UpdateProposalRequest request);
    void withdraw(String email, Long proposalId);
    ProposalResponse accept(String email, Long proposalId);
    PageResponse<ProposalResponse> getMine(String email, int page, int size);
    PageResponse<ProposalResponse> getForOwnedJob(String email, Long jobId, int page, int size);
}
