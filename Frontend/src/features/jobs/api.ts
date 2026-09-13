import { api } from '../../shared/api/client'
import type { Job, PageResult, Skill } from '../../shared/types/domain'

type BackendJob = Omit<Job, 'skills'> & { skills: string[] }
type BackendPage<T> = Omit<PageResult<T>, 'content'> & { content: T[] }
const toJob = (job: BackendJob): Job => ({ ...job, skills: job.skills.map((name, index) => ({ id: index, name })) })

export const jobsApi = {
  list: async (query = '') => {
    const result = await api<BackendPage<BackendJob>>(`/jobs${query}`)
    return { ...result, content: result.content.map(toJob) } as PageResult<Job>
  },
  get: async (id: string | number) => toJob(await api<BackendJob>(`/jobs/${id}`)),
  create: async (payload: object) => toJob(await api<BackendJob>('/jobs', { method: 'POST', body: JSON.stringify(payload) })),
  apply: (id: string | number, payload: object) =>
    api(`/jobs/${id}/proposals`, { method: 'POST', body: JSON.stringify(payload) }),
}

export const suggestedSkills: Skill[] = ['Java', 'Spring Boot', 'React', 'TypeScript', 'Flutter', 'UI/UX']
  .map((name, index) => ({ id: index + 1, name }))
