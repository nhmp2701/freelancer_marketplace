import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getCurrentUser } from '../features/auth/session'
import { api } from '../shared/api/client'

type Profile = {
  id: number; email: string; fullName: string; avatarUrl?: string; bio?: string;
  reputationScore: number; status: string; skills: string[]
}

export function ProfilePage() {
  const session = getCurrentUser()
  const [profile, setProfile] = useState<Profile | null>(null)
  const [error, setError] = useState('')

  useEffect(() => { if (session) api<Profile>(`/users/${session.id}`).then(setProfile).catch(() => setError('Không thể tải hồ sơ. Hãy kiểm tra backend.')) }, [session?.id])

  if (!session) return <div className="page"><section className="container empty-state"><span className="material-symbols-outlined">person</span><h1>Hồ sơ năng lực</h1><p>Đăng nhập để xây dựng portfolio và theo dõi uy tín của bạn.</p><Link className="button button--primary" to="/login">Đăng nhập</Link></section></div>
  if (!profile) return <div className="page"><section className="container empty-state"><h1>{error || 'Đang tải hồ sơ...'}</h1></section></div>

  return <div className="page page--soft"><div className="container profile-layout"><aside className="panel profile-card"><div className="profile-card__cover" /><span className="avatar avatar--large">{profile.fullName[0]}</span><h1>{profile.fullName}</h1><p className="profile-role">{profile.email}</p><div className="reputation"><span className="material-symbols-outlined">workspace_premium</span><div><b>{Number(profile.reputationScore).toFixed(1)} / 5</b><small>Điểm uy tín tổng hợp</small></div></div></aside><div className="profile-content"><section className="panel"><div className="panel-heading"><div><span className="eyebrow">Giới thiệu</span><h2>Năng lực chuyên môn</h2></div><span className="status status--open">{profile.status}</span></div><p className="profile-bio">{profile.bio || 'Chưa có phần giới thiệu.'}</p><div className="tags">{profile.skills.length ? profile.skills.map((skill) => <span className="tag" key={skill}>{skill}</span>) : <span>Chưa có kỹ năng.</span>}</div></section></div></div></div>
}
