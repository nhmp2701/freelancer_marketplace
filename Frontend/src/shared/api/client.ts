const API_URL = import.meta.env.VITE_API_URL ?? '/api/v1'

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code?: string,
  ) {
    super(message)
  }
}

export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('accessToken')
  const response = await fetch(`${API_URL}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...init.headers,
    },
  })

  if (!response.ok) {
    const payload = await response.json().catch(() => null) as Record<string, unknown> | null
    const validation = payload && Object.values(payload).find((value) => typeof value === 'string')
    throw new ApiError(String(payload?.message ?? validation ?? 'Không thể kết nối đến hệ thống'), response.status)
  }
  const body = await response.text()
  return body ? JSON.parse(body) as T : undefined as T
}
