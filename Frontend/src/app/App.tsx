import { Navigate, Route, Routes } from 'react-router-dom'
import { AppShell } from './layouts/AppShell'
import { AuthPage } from '../pages/AuthPage'
import { HomePage } from '../pages/HomePage'
import { JobDetailPage } from '../pages/JobDetailPage'
import { JobsPage } from '../pages/JobsPage'
import { PlaceholderPage } from '../pages/PlaceholderPage'
import { PostProjectPage } from '../pages/PostProjectPage'
import { ProfilePage } from '../pages/ProfilePage'
import { WalletPage } from '../pages/WalletPage'

export function App() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route index element={<HomePage />} />
        <Route path="jobs" element={<JobsPage />} />
        <Route path="jobs/:id" element={<JobDetailPage />} />
        <Route path="post-project" element={<PostProjectPage />} />
        <Route path="wallet" element={<WalletPage />} />
        <Route path="profile" element={<ProfilePage />} />
        <Route path="messages" element={<PlaceholderPage icon="forum" title="Tin nhắn" description="Module hội thoại đã có vị trí trong kiến trúc và sẽ được nối sau luồng job/application." />} />
        <Route path="admin" element={<PlaceholderPage icon="admin_panel_settings" title="Quản trị hệ thống" description="Dashboard admin sẽ dùng các domain users, jobs, transactions và disputes đã thống nhất." />} />
      </Route>
      <Route path="login" element={<AuthPage mode="login" />} />
      <Route path="register" element={<AuthPage mode="register" />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
