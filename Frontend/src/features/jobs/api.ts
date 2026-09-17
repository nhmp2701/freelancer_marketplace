import { api } from "../../shared/api/client";
import type { Job, PageResult, Skill } from "../../shared/types/domain";

type BackendJob = Omit<Job, "skills"> & { skills: string[] };
type BackendPage<T> = Omit<PageResult<T>, "content"> & { content: T[] };
const toJob = (job: BackendJob): Job => ({
  ...job,
  skills: job.skills.map((name, index) => ({ id: index, name })),
});

export const jobsApi = {
  list: async (query = "") => {
    const result = await api<BackendPage<BackendJob>>(`/jobs${query}`);
    return { ...result, content: result.content.map(toJob) } as PageResult<Job>;
  },
  get: async (id: string | number) => toJob(await api<BackendJob>(`/jobs/${id}`)),
  posted: async () => (await api<BackendJob[]>("/jobs/me/posted")).map(toJob),
  accepted: async () => (await api<BackendJob[]>("/jobs/me/accepted")).map(toJob),
  create: async (payload: object) =>
    toJob(
      await api<BackendJob>("/jobs", {
        method: "POST",
        body: JSON.stringify(payload),
      }),
    ),
  apply: (id: string | number, payload: object) =>
    api(`/jobs/${id}/proposals`, {
      method: "POST",
      body: JSON.stringify(payload),
    }),
  submit: async (id: number, payload: object) =>
    toJob(
      await api<BackendJob>(`/jobs/${id}/submit`, {
        method: "POST",
        body: JSON.stringify(payload),
      }),
    ),
  acceptSubmission: async (id: number) =>
    toJob(
      await api<BackendJob>(`/jobs/${id}/accept-submission`, {
        method: "POST",
      }),
    ),
  requestChanges: async (id: number, reason: string) =>
    toJob(
      await api<BackendJob>(`/jobs/${id}/request-changes`, {
        method: "POST",
        body: JSON.stringify({ reason }),
      }),
    ),
  updateProgress: async (id: number, progress: number, note: string) =>
    toJob(
      await api<BackendJob>(`/jobs/${id}/progress`, {
        method: "POST",
        body: JSON.stringify({ progress, note }),
      }),
    ),
};

export const suggestedSkills: Skill[] = [
  "Java",
  "Spring Boot",
  "Kotlin",
  "C#",
  ".NET",
  "Node.js",
  "PHP",
  "Laravel",
  "Python",
  "Django",
  "React",
  "Next.js",
  "Vue.js",
  "Angular",
  "TypeScript",
  "JavaScript",
  "HTML/CSS",
  "Tailwind CSS",
  "Flutter",
  "React Native",
  "Android",
  "iOS",
  "PostgreSQL",
  "MySQL",
  "MongoDB",
  "Redis",
  "Docker",
  "AWS",
  "DevOps",
  "REST API",
  "GraphQL",
  "Data Analysis",
  "Machine Learning",
  "UI/UX",
  "Figma",
  "Graphic Design",
  "SEO",
  "Content Writing",
  "Digital Marketing",
  "Translation",
].map((name, index) => ({ id: index + 1, name }));
