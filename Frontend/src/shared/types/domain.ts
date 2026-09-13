export type Skill = { id: number; name: string }

export type Job = {
  id: number
  title: string
  description: string
  budget: number
  status: 'OPEN' | 'IN_PROGRESS' | 'SUBMITTED' | 'COMPLETED' | 'DISPUTED' | 'CANCELLED'
  deadline?: string | null
  createdAt: string
  clientId: number
  clientName: string
  freelancerId?: number | null
  freelancerName?: string | null
  submissionUrl?: string | null
  submissionNote?: string | null
  skills: Skill[]
}

export type PageResult<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type Wallet = { balance: number; lockedBalance: number }

export type WalletTransaction = {
  id: number
  jobId?: number | null
  type: 'TOPUP' | 'HOLD' | 'RELEASE' | 'REFUND' | 'WITHDRAW'
  amount: number
  balanceAfter: number
  lockedAfter: number
  createdAt: string
}

export type AuthResult = {
  accessToken: string
  tokenType: string
  userId: number
  email: string
  fullName: string
  role: string
}
