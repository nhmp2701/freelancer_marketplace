export interface Skill {
  id: number;
  name: string;
}

export type Job = {
  id: number;
  title: string;
  description: string;
  budget: number;
  status: "OPEN" | "IN_PROGRESS" | "SUBMITTED" | "COMPLETED" | "DISPUTED" | "CANCELLED";
  deadline?: string | null;
  createdAt: string;
  clientId: number;
  clientName: string;
  freelancerId?: number | null;
  freelancerName?: string | null;
  submissionUrl?: string | null;
  submissionNote?: string | null;
  revisionNote?: string | null;
  progress?: number;
  progressNote?: string | null;
  progressUpdatedAt?: string | null;
  skills: Skill[];
};

export type PageResult<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export interface Wallet {
  balance: number;
  lockedBalance: number;
}

export type WalletTransaction = {
  id: number;
  jobId?: number | null;
  type: "TOPUP" | "HOLD" | "RELEASE" | "REFUND" | "WITHDRAW";
  amount: number;
  balanceAfter: number;
  lockedAfter: number;
  createdAt: string;
};

export type AuthResult = {
  accessToken: string;
  tokenType: string;
  userId: number;
  email: string;
  fullName: string;
  role: string;
};

export type Proposal = {
  id: number;
  jobId: number;
  jobTitle: string;
  freelancerId: number;
  freelancerName: string;
  coverLetter: string;
  bidAmount: number;
  deliveryDays: number;
  status: "PENDING" | "ACCEPTED" | "REJECTED" | "WITHDRAWN";
  createdAt: string;
  updatedAt: string;
};
