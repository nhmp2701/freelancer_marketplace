import { FormEvent, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { jobsApi } from '../features/jobs/api'
import { statusLabel, initials } from '../shared/components/JobCard'
import { ApiError } from '../shared/api/client'
import type { Job } from '../shared/types/domain'

const money = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 })

export function JobDetailPage() {
  const { id = '1' } = useParams()
  const [job, setJob] = useState<Job | null>(null)
  const [message, setMessage] = useState('')
  const [loadError, setLoadError] = useState('')
  const [sending, setSending] = useState(false)

  useEffect(() => {
    jobsApi.get(id).then(setJob).catch((reason) => setLoadError(reason instanceof ApiError ? reason.message : 'Không thể tải công việc.'))
  }, [id])

  const apply = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const form = event.currentTarget
    const data = new FormData(form)
    setSending(true)
    setMessage('')
    try {
      await jobsApi.apply(id, { bidAmount: Number(data.get('proposedPrice')), coverLetter: data.get('coverLetter'), deliveryDays: Number(data.get('deliveryDays')) })
      setMessage('Proposal đã được gửi. Client có thể xem và phản hồi ngay.')
      form.reset()
    } catch (reason) {
      setMessage(reason instanceof ApiError ? reason.message : 'Hãy đăng nhập và khởi động backend để gửi proposal.')
    } finally {
      setSending(false)
    }
  }

  if (!job) return <div className="container empty-state"><h1>{loadError || 'Đang tải công việc...'}</h1><Link to="/jobs">Quay lại danh sách</Link></div>

  return (
    <div className="page page--soft"><div className="container detail-grid">
      <div className="detail-main">
        <Link to="/jobs" className="back-link"><span className="material-symbols-outlined">arrow_back</span> Tất cả công việc</Link>
        <article className="panel job-detail">
          <div className="job-detail__heading"><div><span className={`status status--${job.status.toLowerCase()}`}>{statusLabel(job.status)}</span><h1>{job.title}</h1></div><div className="budget-block"><span>Ngân sách cố định</span><strong>{money.format(job.budget)}</strong></div></div>
          <div className="detail-metrics"><div><span>Ngày đăng</span><b>{new Date(job.createdAt).toLocaleDateString('vi-VN')}</b></div><div><span>Hạn hoàn thành</span><b>{job.deadline ? new Date(job.deadline).toLocaleDateString('vi-VN') : 'Thỏa thuận'}</b></div><div><span>Hình thức</span><b>Làm việc từ xa</b></div></div>
          <section className="prose"><h2>Mô tả dự án</h2><p>{job.description}</p><h2>Kết quả mong đợi</h2><ul><li>Source code rõ ràng, có hướng dẫn chạy.</li><li>Cập nhật tiến độ theo các mốc đã thống nhất.</li><li>Bàn giao và hỗ trợ nghiệm thu sản phẩm.</li></ul></section>
          <div className="tags">{job.skills.map((skill) => <span className="tag" key={skill.id}>{skill.name}</span>)}</div>
        </article>
        <article className="panel transparency-panel"><span className="material-symbols-outlined">account_tree</span><div><h2>Quy trình minh bạch</h2><p>OPEN → IN_PROGRESS → SUBMITTED → COMPLETED. Ngân sách chỉ được giải ngân sau nghiệm thu.</p></div></article>
      </div>
      <aside className="detail-sidebar">
        <section className="panel client-card"><span className="eyebrow">Về khách hàng</span><div className="client-card__identity"><span className="avatar">{initials(job.clientName)}</span><div><h3>{job.clientName}</h3><p>TP. Hồ Chí Minh</p></div></div><div className="client-stats"><span><b>4.8</b> đánh giá</span><span><b>15</b> dự án</span><span><b>100%</b> thanh toán</span></div></section>
        <section className="panel proposal-card">
          <span className="eyebrow">Proposal của bạn</span><h2>Đề xuất hợp tác</h2><p>Chia sẻ mức giá và cách bạn sẽ giải quyết dự án.</p>
          <form className="form-stack" onSubmit={apply}><label>Giá đề xuất (VNĐ)<input name="proposedPrice" type="number" min="1" required defaultValue={job.budget} /></label><label>Thời gian thực hiện (ngày)<input name="deliveryDays" type="number" min="1" required defaultValue="7" /></label><label>Thư giới thiệu<textarea name="coverLetter" required rows={6} placeholder="Kinh nghiệm liên quan và kế hoạch thực hiện..." /></label>{message && <div className="alert">{message}</div>}<button className="button button--action button--large" disabled={sending}>{sending ? 'Đang gửi...' : 'Gửi proposal'}</button><small>Phí nền tảng chỉ áp dụng khi dự án hoàn thành.</small></form>
        </section>
      </aside>
    </div></div>
  )
}
