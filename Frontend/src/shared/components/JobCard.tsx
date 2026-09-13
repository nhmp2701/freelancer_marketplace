import { Link } from 'react-router-dom'
import type { Job } from '../types/domain'

const money = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 })

export function JobCard({ job, featured = false }: { job: Job; featured?: boolean }) {
  return (
    <article className={`job-card ${featured ? 'job-card--featured' : ''}`}>
      <div className="job-card__topline">
        <span className={`status status--${job.status.toLowerCase()}`}>{statusLabel(job.status)}</span>
        <span className="muted">{relativeDate(job.createdAt)}</span>
      </div>
      <Link to={`/jobs/${job.id}`} className="job-card__title">{job.title}</Link>
      <p className="job-card__description">{job.description}</p>
      <div className="tags">
        {job.skills.map((skill) => <span className="tag" key={skill.id}>{skill.name}</span>)}
      </div>
      <div className="job-card__footer">
        <div>
          <span className="eyebrow">Ngân sách</span>
          <strong>{money.format(job.budget)}</strong>
        </div>
        <div className="job-card__client">
          <span className="avatar avatar--small">{initials(job.clientName)}</span>
          <span>{job.clientName}</span>
        </div>
      </div>
    </article>
  )
}

export function statusLabel(status: Job['status']) {
  return ({
    OPEN: 'Đang mở', IN_PROGRESS: 'Đang thực hiện', SUBMITTED: 'Chờ nghiệm thu',
    COMPLETED: 'Hoàn thành', DISPUTED: 'Tranh chấp', CANCELLED: 'Đã hủy',
  })[status]
}

export function initials(name: string) {
  return name.split(' ').slice(-2).map((part) => part[0]).join('').toUpperCase()
}

function relativeDate(value: string) {
  const days = Math.max(0, Math.round((Date.now() - new Date(value).getTime()) / 86400000))
  if (days === 0) return 'Hôm nay'
  if (days === 1) return 'Hôm qua'
  return `${days} ngày trước`
}
