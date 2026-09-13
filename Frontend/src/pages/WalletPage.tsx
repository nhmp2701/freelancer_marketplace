import { FormEvent, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getCurrentUser } from '../features/auth/session'
import { walletApi } from '../features/wallet/api'
import type { Wallet, WalletTransaction } from '../shared/types/domain'

const money = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 })

export function WalletPage() {
  const user = getCurrentUser()
  const [wallet, setWallet] = useState<Wallet>({ balance: 0, lockedBalance: 0 })
  const [transactions, setTransactions] = useState<WalletTransaction[]>([])
  const [notice, setNotice] = useState('')

  const load = () => Promise.all([walletApi.get(), walletApi.history()]).then(([walletData, history]) => {
    setWallet(walletData); setTransactions(history)
  }).catch(() => setNotice('Không thể tải ví. Hãy kiểm tra backend và phiên đăng nhập.'))

  useEffect(() => { if (user) void load() }, [])

  const topUp = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const amount = Number(new FormData(event.currentTarget).get('amount'))
    try {
      setWallet(await walletApi.topUp(amount))
      setNotice('Nạp tiền demo thành công.')
      await load()
      event.currentTarget.reset()
    } catch { setNotice('Không thể nạp tiền demo lúc này.') }
  }

  if (!user) return <div className="page"><section className="container empty-state"><span className="material-symbols-outlined">account_balance_wallet</span><h1>Đăng nhập để quản lý ví</h1><p>Số dư và lịch sử giao dịch chỉ hiển thị cho chủ tài khoản.</p><Link className="button button--primary" to="/login">Đăng nhập</Link></section></div>

  return <div className="page page--soft"><div className="container wallet-page"><div className="page-heading page-heading--row"><div><span className="eyebrow">Ví nội bộ</span><h1>Tài chính minh bạch trong từng dự án</h1><p>Theo dõi số dư khả dụng và ngân sách đang được Escrow bảo vệ.</p></div><button className="button button--outline"><span className="material-symbols-outlined">download</span> Xuất sao kê</button></div><section className="wallet-overview"><article className="balance-card balance-card--primary"><span>Số dư khả dụng</span><strong>{money.format(wallet.balance)}</strong><small>Sẵn sàng sử dụng</small><span className="material-symbols-outlined balance-card__icon">account_balance_wallet</span></article><article className="balance-card"><span>Đang khóa trong Escrow</span><strong>{money.format(wallet.lockedBalance)}</strong><small>Được giải ngân theo nghiệm thu</small><span className="material-symbols-outlined balance-card__icon">lock</span></article><form className="panel quick-topup" onSubmit={topUp}><div><span className="eyebrow">Nạp tiền demo</span><b>Thử luồng ví an toàn</b></div><input name="amount" type="number" min="10000" required placeholder="Số tiền" /><button className="button button--action">Nạp vào ví</button></form></section>{notice && <div className="alert">{notice}</div>}<section className="panel transaction-panel"><div className="panel-heading"><div><span className="eyebrow">Ledger</span><h2>Giao dịch gần đây</h2></div><span className="pill">Append-only</span></div>{transactions.length ? <div className="transaction-list">{transactions.map((tx) => <div className="transaction" key={tx.id}><span className={`transaction__icon transaction__icon--${tx.type.toLowerCase()}`}><span className="material-symbols-outlined">{tx.type === 'TOPUP' ? 'add_card' : tx.type === 'HOLD' ? 'lock' : 'payments'}</span></span><div><b>{transactionName(tx.type)}</b><small>{new Date(tx.createdAt).toLocaleString('vi-VN')}{tx.jobId ? ` · Job #${tx.jobId}` : ''}</small></div><strong className={tx.amount >= 0 ? 'money-positive' : 'money-negative'}>{tx.amount >= 0 ? '+' : ''}{money.format(tx.amount)}</strong></div>)}</div> : <div className="empty-inline">Chưa có giao dịch. Bạn có thể thử nạp tiền demo.</div>}</section></div></div>
}

function transactionName(type: WalletTransaction['type']) {
  return ({ TOPUP: 'Nạp tiền demo', HOLD: 'Khóa ngân sách', RELEASE: 'Giải ngân', REFUND: 'Hoàn tiền', WITHDRAW: 'Rút tiền' })[type]
}
