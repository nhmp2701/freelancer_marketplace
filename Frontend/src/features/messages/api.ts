import { api } from "../../shared/api/client";
import type { PageResult } from "../../shared/types/domain";

export type Conversation = {
  id: number;
  jobId: number;
  jobTitle: string;
  partnerId: number;
  partnerName: string;
  unreadCount: number;
  lastMessage?: string | null;
  lastMessageAt?: string | null;
};
export type Message = {
  id: number;
  senderId: number;
  senderName: string;
  body: string;
  createdAt: string;
};

export const messagesApi = {
  list: () => api<Conversation[]>("/conversations"),
  open: (jobId: number) => api<Conversation>(`/jobs/${jobId}/conversation`, { method: "POST" }),
  messages: (id: number) => api<PageResult<Message>>(`/conversations/${id}/messages?size=100`),
  send: (id: number, body: string) =>
    api<Message>(`/conversations/${id}/messages`, {
      method: "POST",
      body: JSON.stringify({ body }),
    }),
  read: (id: number) => api<void>(`/conversations/${id}/read`, { method: "POST" }),
};
