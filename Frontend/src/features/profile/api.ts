import { api } from "../../shared/api/client";

export type Profile = {
  id: number;
  email: string;
  fullName: string;
  avatarUrl?: string;
  bio?: string;
  reputationScore: number;
  status: string;
  skills: string[];
};

export const profileApi = {
  get: (id: number) => api<Profile>(`/users/${id}`),
  update: (payload: Pick<Profile, "fullName" | "bio" | "avatarUrl">) =>
    api<Profile>("/users/me", { method: "PUT", body: JSON.stringify(payload) }),
  addSkill: (name: string) =>
    api<void>("/users/me/skills", {
      method: "POST",
      body: JSON.stringify({ name }),
    }),
  removeSkill: (name: string) =>
    api<void>(`/users/me/skills/${encodeURIComponent(name)}`, {
      method: "DELETE",
    }),
};
