import type { AuthResult } from '../../shared/types/domain'

export function saveSession(result: AuthResult) {
  localStorage.setItem('accessToken', result.accessToken)
  localStorage.setItem('currentUser', JSON.stringify({
    id: result.userId,
    fullName: result.fullName,
    role: result.role,
  }))
  window.dispatchEvent(new Event('session-changed'))
}

export function clearSession() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('currentUser')
  window.dispatchEvent(new Event('session-changed'))
}

export function getCurrentUser(): { id: number; fullName: string; role: string } | null {
  const value = localStorage.getItem('currentUser')
  return value ? JSON.parse(value) : null
}
