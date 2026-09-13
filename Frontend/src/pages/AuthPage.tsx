import { FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { saveSession } from '../features/auth/session'
import { api, ApiError } from '../shared/api/client'
import type { AuthResult } from '../shared/types/domain'

export function AuthPage({ mode }: { mode: 'login' | 'register' }) {
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setLoading(true)
    setError('')
    const data = new FormData(event.currentTarget)
    const payload = Object.fromEntries(data.entries())
    try {
      const result = await api<AuthResult>(`/auth/${mode === 'login' ? 'login' : 'register'}`, {
        method: 'POST', body: JSON.stringify(payload),
      })
      saveSession(result)
      navigate('/jobs')
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Backend chưa sẵn sàng. Hãy khởi động Spring Boot trước.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="auth-page">
      <Link to="/" className="brand auth-page__brand"><span className="brand__mark">LK</span><span>Liên Kết Việt</span></Link>
      <section className="auth-story">
        <span className="eyebrow eyebrow--light">Nơi năng lực tạo nên uy tín</span>
        <h1>{mode === 'login' ? 'Chào mừng bạn quay lại.' : 'Bắt đầu một hành trình đáng tin cậy.'}</h1>
        <p>Một tài khoản duy nhất để thuê chuyên gia, nhận dự án và quản lý dòng tiền Escrow minh bạch.</p>
        <div className="auth-story__quote"><span className="material-symbols-outlined">format_quote</span><blockquote>Liên Kết Việt giúp đội ngũ của tôi tìm đúng kỹ sư chỉ trong ba ngày và kiểm soát từng mốc thanh toán.</blockquote><small>Trần Hoàng, Founder Nova Labs</small></div>
      </section>
      <section className="auth-panel">
        <div className="auth-form-wrap">
          <span className="eyebrow">{mode === 'login' ? 'Đăng nhập' : 'Tạo tài khoản'}</span>
          <h2>{mode === 'login' ? 'Tiếp tục công việc của bạn' : 'Tham gia Liên Kết Việt'}</h2>
          <p>{mode === 'login' ? 'Tài khoản demo: client@lienketviet.vn / Password123' : 'Bạn có thể vừa đăng việc vừa nhận việc.'}</p>
          <form className="form-stack form-stack--large" onSubmit={submit}>
            {mode === 'register' && <label>Họ và tên<input name="fullName" autoComplete="name" required maxLength={150} placeholder="Nguyễn Minh Anh" /></label>}
            <label>Email<input name="email" type="email" autoComplete="email" required defaultValue={mode === 'login' ? 'client@lienketviet.vn' : ''} placeholder="ban@email.com" /></label>
            <label>Mật khẩu<input name="password" type="password" autoComplete={mode === 'login' ? 'current-password' : 'new-password'} required minLength={8} defaultValue={mode === 'login' ? 'Password123' : ''} placeholder="Tối thiểu 8 ký tự" /></label>
            {error && <div className="alert alert--error">{error}</div>}
            <button className="button button--action button--large" disabled={loading}>{loading ? 'Đang xử lý...' : mode === 'login' ? 'Đăng nhập' : 'Tạo tài khoản'}</button>
          </form>
          <p className="auth-switch">{mode === 'login' ? 'Chưa có tài khoản?' : 'Đã có tài khoản?'} <Link to={mode === 'login' ? '/register' : '/login'}>{mode === 'login' ? 'Đăng ký ngay' : 'Đăng nhập'}</Link></p>
        </div>
      </section>
    </main>
  )
}
