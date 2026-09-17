import { useEffect, useState } from "react";
import { Link, NavLink, Outlet } from "react-router-dom";
import { clearSession, getCurrentUser } from "../../features/auth/session";
import { messagesApi } from "../../features/messages/api";
import { jobsApi } from "../../features/jobs/api";
import { proposalSeenAt, proposalsApi } from "../../features/proposals/api";

export function AppShell() {
  const [user, setUser] = useState(getCurrentUser());
  const [unread, setUnread] = useState(0);
  const [proposalUnread, setProposalUnread] = useState(0);

  useEffect(() => {
    // Đồng bộ người dùng khi đăng nhập, cập nhật hồ sơ hoặc đăng xuất.
    const update = () => setUser(getCurrentUser());
    window.addEventListener("session-changed", update);
    return () => window.removeEventListener("session-changed", update);
  }, []);

  useEffect(() => {
    // Đặt polling ở layout để badge tin nhắn vẫn cập nhật khi đang xem trang khác.
    if (!user) {
      setUnread(0);
      return;
    }
    const refresh = () =>
      messagesApi
        .list()
        .then((items) => setUnread(items.reduce((sum, item) => sum + item.unreadCount, 0)))
        .catch(() => undefined);
    void refresh();
    const timer = window.setInterval(() => {
      if (!document.hidden) void refresh();
    }, 5000);
    window.addEventListener("messages-read", refresh);
    return () => {
      window.clearInterval(timer);
      window.removeEventListener("messages-read", refresh);
    };
  }, [user?.id]);

  const badge = unread >= 5 ? "5+" : String(unread);
  useEffect(() => {
    // Gộp thay đổi đề xuất đã gửi và đề xuất nhận được thành một badge điều hướng.
    if (!user) {
      setProposalUnread(0);
      return;
    }
    const refresh = async () => {
      try {
        const [mine, posted] = await Promise.all([proposalsApi.mine(), jobsApi.posted()]);
        const received = (
          await Promise.all(posted.map((job) => proposalsApi.forJob(job.id)))
        ).flatMap((page) => page.content);
        const seen = proposalSeenAt(user.id);
        setProposalUnread(
          [...mine.content, ...received].filter((item) => Date.parse(item.updatedAt) > seen).length,
        );
      } catch {
        /* badge is best effort */
      }
    };
    void refresh();
    const timer = window.setInterval(() => {
      if (!document.hidden) void refresh();
    }, 5000);
    const clear = () => setProposalUnread(0);
    window.addEventListener("proposals-read", clear);
    return () => {
      window.clearInterval(timer);
      window.removeEventListener("proposals-read", clear);
    };
  }, [user?.id]);
  const proposalBadge = proposalUnread >= 5 ? "5+" : String(proposalUnread);

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
            <NavLink className="nav-with-badge" to="/proposals">
              Đề xuất
              {proposalUnread > 0 && <span className="notification-badge">{proposalBadge}</span>}
            </NavLink>
            <NavLink to="/projects">Dự án</NavLink>
            <NavLink to="/rankings">Xếp hạng</NavLink>
            <NavLink to="/wallet">Ví</NavLink>
            <NavLink className="nav-with-badge" to="/messages">
              Tin nhắn
              {unread > 0 && <span className="notification-badge">{badge}</span>}
            </NavLink>
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
                <Link className="button button--ghost topbar__login" to="/login">
                  Đăng nhập
                </Link>
                <Link className="button button--primary" to="/register">
                  Tham gia
                </Link>
              </>
            )}
          </div>
        </div>
      </header>
      <main className="main-content">
        <Outlet />
      </main>
      <nav className="mobile-nav" aria-label="Điều hướng di động">
        <NavLink to="/jobs">
          <span className="material-symbols-outlined">work</span>
          <small>Tìm việc</small>
        </NavLink>
        <NavLink to="/post-project">
          <span className="material-symbols-outlined">add_box</span>
          <small>Đăng dự án</small>
        </NavLink>
        <NavLink className="nav-with-badge" to="/proposals">
          <span className="material-symbols-outlined">description</span>
          <small>Đề xuất</small>
          {proposalUnread > 0 && <span className="notification-badge">{proposalBadge}</span>}
        </NavLink>
        <NavLink to="/wallet">
          <span className="material-symbols-outlined">account_balance_wallet</span>
          <small>Ví</small>
        </NavLink>
        <NavLink className="nav-with-badge" to="/messages">
          <span className="material-symbols-outlined">forum</span>
          <small>Tin nhắn</small>
          {unread > 0 && <span className="notification-badge">{badge}</span>}
        </NavLink>
      </nav>
      <footer className="footer">
        <div className="container footer__inner">
          <div>
            <strong>Liên Kết Việt</strong>
            <p>Kết nối đúng chuyên gia. Giao dịch minh bạch.</p>
          </div>
          <div className="footer__links">
            <a href="#process">Quy trình</a>
            <a href="#trust">Ký quỹ an toàn</a>
            <a href="#support">Hỗ trợ</a>
          </div>
        </div>
      </footer>
    </div>
  );
}
