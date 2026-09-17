import { Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "./layouts/AppShell";
import { AuthPage } from "../pages/AuthPage";
import { HomePage } from "../pages/HomePage";
import { JobDetailPage } from "../pages/JobDetailPage";
import { JobsPage } from "../pages/JobsPage";
import { PlaceholderPage } from "../pages/PlaceholderPage";
import { PostProjectPage } from "../pages/PostProjectPage";
import { ProfilePage } from "../pages/ProfilePage";
import { WalletPage } from "../pages/WalletPage";
import { ProposalsPage } from "../pages/ProposalsPage";
import { ProjectsPage } from "../pages/ProjectsPage";
import { MessagesPage } from "../pages/MessagesPage";
import { RankingsPage } from "../pages/RankingsPage";

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
        <Route path="profile/:userId" element={<ProfilePage />} />
        <Route path="proposals" element={<ProposalsPage />} />
        <Route path="projects" element={<ProjectsPage />} />
        <Route path="rankings" element={<RankingsPage />} />
        <Route path="messages" element={<MessagesPage />} />
        <Route
          path="admin"
          element={
            <PlaceholderPage
              icon="admin_panel_settings"
              title="Quản trị hệ thống"
              description="Trang quản trị sẽ dùng dữ liệu người dùng, dự án, giao dịch và khiếu nại đã thống nhất."
            />
          }
        />
      </Route>
      <Route path="login" element={<AuthPage mode="login" />} />
      <Route path="register" element={<AuthPage mode="register" />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
