import { api } from "../../shared/api/client";
import type { PageResult, Proposal } from "../../shared/types/domain";

export const proposalsApi = {
  mine: () => api<PageResult<Proposal>>("/proposals/me?size=100"),
  forJob: (jobId: number) => api<PageResult<Proposal>>(`/jobs/${jobId}/proposals?size=100`),
  accept: (proposalId: number) =>
    api<Proposal>(`/proposals/${proposalId}/accept`, { method: "POST" }),
  withdraw: (proposalId: number) => api<void>(`/proposals/${proposalId}`, { method: "DELETE" }),
};

const seenKey = (userId: number) => `proposal-seen-${userId}`;
export const proposalSeenAt = (userId: number) =>
  Number(localStorage.getItem(seenKey(userId)) ?? 0);
export const markProposalsSeen = (userId: number) => {
  localStorage.setItem(seenKey(userId), String(Date.now()));
  window.dispatchEvent(new Event("proposals-read"));
};
