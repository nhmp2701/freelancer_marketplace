import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet } from 'react-router-dom'
import { clearSession, getCurrentUser } from '../../features/auth/session'

export function AppShell() {
  const [user, setUser] = useState(getCurrentUser())

  useEffect(() => {
    const update = () => setUser(getCurrentUser())
    window.addEventListener('session-changed', update)
    return () => window.removeEventListener('session-changed', update)
  }, [])

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="container topbar__inner">
          <Link to="/" className="brand">
            <span className="brand__mark">LK</span>
            <span>Liên Kết Việt</span>
          </Link>
          <nav className="main-nav" aria-label="Điều hướng chính">
            <NavLink to="/jobs">Tìm việc</NavLink>
            <NavLink to="/post-project">Đăng dự án</NavLink>
            <NavLink to="/wallet">Ví</NavLink>
            <NavLink to="/messages">Tin nhắn</NavLink>
          </nav>
          <div className="topbar__actions">
            {user ? (
              <>
                <Link className="user-chip" to="/profile">
                  <span className="avatar avatar--small">{user.fullName[0]}</span>
                  <span>{user.fullName}</span>
                </Link>
                <button className="icon-button" title="Đăng xuất" onClick={() => clearSession()}>
                  <span className="material-symbols-outlined">logout</span>
                </button>
              </>
            ) : (
              <>
                <Link className="button button--ghost topbar__login" to="/login">Đăng nhập</Link>
                <Link className="button button--primary" to="/register">Tham gia</Link>
              </>
            )}
          </div>
        </div>
      </header>
      <main className="main-content"><Outlet /></main>
      <nav className="mobile-nav" aria-label="Điều hướng di động">
        <NavLink to="/jobs"><span className="material-symbols-outlined">work</span><small>Tìm việc</small></NavLink>
        <NavLink to="/post-project"><span className="material-symbols-outlined">add_box</span><small>Đăng dự án</small></NavLink>
        <NavLink to="/wallet"><span className="material-symbols-outlined">account_balance_wallet</span><small>Ví</small></NavLink>
        <NavLink to="/messages"><span className="material-symbols-outlined">forum</span><small>Tin nhắn</small></NavLink>
      </nav>
      <footer className="footer">
        <div className="container footer__inner">
          <div><strong>Liên Kết Việt</strong><p>Kết nối đúng chuyên gia. Giao dịch minh bạch.</p></div>
          <div className="footer__links"><a href="#process">Quy trình</a><a href="#trust">An toàn Escrow</a><a href="#support">Hỗ trợ</a></div>
        </div>
      </footer>
    </div>
  )
}
