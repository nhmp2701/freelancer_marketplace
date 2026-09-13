import { FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { jobsApi, suggestedSkills } from '../features/jobs/api'
import { ApiError } from '../shared/api/client'
import type { Skill } from '../shared/types/domain'

export function PostProjectPage() {
  const navigate = useNavigate()
  const [skills] = useState<Skill[]>(suggestedSkills)
  const [selected, setSelected] = useState<number[]>([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    setLoading(true)
    setError('')
    try {
      const job = await jobsApi.create({
        title: data.get('title'), description: data.get('description'), budget: Number(data.get('budget')),
        deadline: String(data.get('deadline')), skills: skills.filter((skill) => selected.includes(skill.id)).map((skill) => skill.name),
      })
      navigate(`/jobs/${job.id}`)
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Hãy đăng nhập bằng tài khoản client và khởi động backend.')
    } finally { setLoading(false) }
  }

  const toggle = (id: number) => setSelected((current) => current.includes(id) ? current.filter((item) => item !== id) : [...current, id])

  return <div className="page page--soft"><div className="container form-page"><header><span className="eyebrow">Dành cho khách hàng</span><h1>Biến ý tưởng thành một brief rõ ràng</h1><p>Thông tin càng cụ thể, proposal bạn nhận được càng phù hợp.</p></header><form className="panel project-form" onSubmit={submit}><div className="form-section"><span className="form-section__number">01</span><div><h2>Tổng quan dự án</h2><p>Đặt tiêu đề ngắn gọn và mô tả kết quả bạn cần.</p></div></div><label>Tiêu đề dự án<input name="title" required maxLength={200} placeholder="VD: Xây dựng ứng dụng quản lý chi tiêu" /></label><label>Mô tả chi tiết<textarea name="description" required rows={8} maxLength={5000} placeholder="Bối cảnh, phạm vi, kết quả bàn giao và tiêu chí nghiệm thu..." /></label><div className="form-section form-section--spaced"><span className="form-section__number">02</span><div><h2>Ngân sách và thời gian</h2><p>Ngân sách sẽ được giữ trong Escrow khi bạn chọn freelancer.</p></div></div><div className="form-grid"><label>Ngân sách (VNĐ)<input name="budget" type="number" min="10000" required placeholder="20000000" /></label><label>Hạn hoàn thành<input name="deadline" type="datetime-local" required /></label></div><label>Kỹ năng cần thiết<div className="skill-picker">{skills.map((skill) => <button type="button" className={selected.includes(skill.id) ? 'tag tag--selected' : 'tag'} key={skill.id} onClick={() => toggle(skill.id)}>{skill.name}</button>)}</div></label>{error && <div className="alert alert--error">{error}</div>}<div className="form-actions"><button type="button" className="button button--ghost" onClick={() => navigate(-1)}>Hủy</button><button className="button button--action button--large" disabled={loading}>{loading ? 'Đang đăng...' : 'Đăng dự án'}</button></div></form></div></div>
}
