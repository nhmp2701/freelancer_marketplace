import { api } from "../../shared/api/client";

export type Review = {
  id: number;
  jobId: number;
  jobTitle: string;
  reviewerId: number;
  reviewerName: string;
  revieweeId: number;
  rating: number;
  comment?: string;
  createdAt: string;
};
export type Ranking = {
  userId: number;
  fullName: string;
  avatarUrl?: string;
  averageRating: number;
  reviewCount: number;
  completedJobs: number;
};

export const reviewsApi = {
  create: (jobId: number, rating: number, comment: string) =>
    api<Review>(`/jobs/${jobId}/reviews`, {
      method: "POST",
      body: JSON.stringify({ rating, comment }),
    }),
  forUser: (userId: number) => api<Review[]>(`/users/${userId}/reviews`),
  mine: () => api<Review[]>("/reviews/me"),
  rankings: (mode: "monthly" | "trusted") => api<Ranking[]>(`/rankings/freelancers?mode=${mode}`),
};
