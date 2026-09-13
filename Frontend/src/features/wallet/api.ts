import { api } from '../../shared/api/client'
import type { Wallet, WalletTransaction } from '../../shared/types/domain'

export const walletApi = {
  get: () => api<Wallet>('/wallet'),
  history: () => api<WalletTransaction[]>('/wallet/transactions'),
  topUp: (amount: number) => api<Wallet>('/wallet/topup', { method: 'POST', body: JSON.stringify({ amount }) }),
}
