import { FormEvent, useCallback, useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { getCurrentUser } from "../features/auth/session";
import { messagesApi, type Conversation, type Message } from "../features/messages/api";

export function MessagesPage() {
  const user = getCurrentUser();
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [selected, setSelected] = useState<number | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [notice, setNotice] = useState("");
  const listRef = useRef<HTMLDivElement>(null);
  // Đồng bộ danh sách và ưu tiên hội thoại vừa có hoạt động mới nhất.
  const loadConversations = useCallback(
    () =>
      messagesApi
        .list()
        .then((items) => {
          setConversations(
            items.sort(
              (a, b) => Date.parse(b.lastMessageAt ?? "") - Date.parse(a.lastMessageAt ?? ""),
            ),
          );
          setSelected((value) => value ?? items[0]?.id ?? null);
        })
        .catch(() => setNotice("Không thể tải hội thoại.")),
    [],
  );
  // Tải nội dung hội thoại đang chọn và đánh dấu đã đọc sau khi hiển thị thành công.
  const loadMessages = useCallback(async () => {
    if (!selected) return;
    try {
      const page = await messagesApi.messages(selected);
      setMessages(page.content);
      await messagesApi.read(selected);
      setConversations((items) =>
        items.map((item) => (item.id === selected ? { ...item, unreadCount: 0 } : item)),
      );
      window.dispatchEvent(new Event("messages-read"));
    } catch {
      setNotice("Không thể tải tin nhắn.");
    }
  }, [selected]);

  useEffect(() => {
    if (!user) return;
    void loadConversations();
    const timer = window.setInterval(() => {
      if (!document.hidden) void loadConversations();
    }, 4000);
    return () => window.clearInterval(timer);
  }, [user?.id, loadConversations]);
  useEffect(() => {
    void loadMessages();
    const timer = window.setInterval(() => {
      if (!document.hidden) void loadMessages();
    }, 2000);
    return () => window.clearInterval(timer);
  }, [loadMessages]);
  useEffect(() => {
    listRef.current?.scrollTo({
      top: listRef.current.scrollHeight,
      behavior: "smooth",
    });
  }, [messages]);
  // Gửi tin nhắn qua API, sau đó đồng bộ cả nội dung lẫn bản xem trước bên trái.
  const send = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!selected) return;
    const form = event.currentTarget;
    const body = String(new FormData(form).get("body")).trim();
    if (!body) return;
    try {
      await messagesApi.send(selected, body);
      form.reset();
      await Promise.all([loadMessages(), loadConversations()]);
    } catch {
      setNotice("Không thể gửi tin nhắn.");
    }
  };
  if (!user)
    return (
      <div className="page">
        <section className="container empty-state">
          <h1>Tin nhắn</h1>
          <p>Đăng nhập để trao đổi với đối tác trong dự án.</p>
          <Link className="button button--primary" to="/login">
            Đăng nhập
          </Link>
        </section>
      </div>
    );
  const active = conversations.find(({ id }) => id === selected);
  return (
    <div className="page page--soft">
      <div className="container messages-layout">
        <aside className="panel conversation-list">
          <div className="conversation-list__header">
            <div>
              <span className="eyebrow">Kết nối</span>
              <h2>Tin nhắn</h2>
            </div>
            <span className="material-symbols-outlined">chat</span>
          </div>
          {conversations.map((item) => (
            <button
              key={item.id}
              className={
                item.id === selected
                  ? "conversation-item conversation-item--active"
                  : "conversation-item"
              }
              onClick={() => setSelected(item.id)}
            >
              <span className="avatar conversation-avatar">{item.jobTitle[0]}</span>
              <span className="conversation-copy">
                <b>{item.jobTitle}</b>
                <small>{item.lastMessage || "Chưa có tin nhắn"}</small>
              </span>
              <span className="conversation-meta">
                <time>
                  {item.lastMessageAt
                    ? new Date(item.lastMessageAt).toLocaleTimeString("vi-VN", {
                        hour: "2-digit",
                        minute: "2-digit",
                      })
                    : ""}
                </time>
                {item.unreadCount > 0 && (
                  <span className="notification-badge notification-badge--inline">
                    {item.unreadCount >= 5 ? "5+" : item.unreadCount}
                  </span>
                )}
              </span>
            </button>
          ))}
          {!conversations.length && (
            <div className="empty-inline">
              Hội thoại xuất hiện khi một đề xuất hợp tác được chấp nhận.
            </div>
          )}
        </aside>
        <section className="panel chat-panel">
          {notice && <div className="alert">{notice}</div>}
          {active ? (
            <>
              <div className="chat-header">
                <span className="avatar">{active.partnerName[0]}</span>
                <div>
                  <h2>{active.partnerName}</h2>
                  <small>
                    <span className="online-dot" /> {active.jobTitle}
                  </small>
                </div>
                <Link className="icon-button" title="Xem dự án" to={`/jobs/${active.jobId}`}>
                  <span className="material-symbols-outlined">open_in_new</span>
                </Link>
              </div>
              <div className="message-list" ref={listRef}>
                {messages.map((message) => (
                  <article
                    key={message.id}
                    className={message.senderId === user.id ? "message message--mine" : "message"}
                  >
                    {message.senderId !== user.id && <b>{message.senderName}</b>}
                    <p>{message.body}</p>
                    <small>
                      {new Date(message.createdAt).toLocaleTimeString("vi-VN", {
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
                    </small>
                  </article>
                ))}
              </div>
              <form className="message-composer" onSubmit={send}>
                <input
                  name="body"
                  required
                  maxLength={4000}
                  autoComplete="off"
                  aria-label="Nội dung tin nhắn"
                  placeholder="Nhập tin nhắn..."
                />
                <button className="button button--action" aria-label="Gửi tin nhắn">
                  <span className="material-symbols-outlined">send</span>
                  <span>Gửi</span>
                </button>
              </form>
            </>
          ) : (
            <div className="empty-inline">Chọn một hội thoại để bắt đầu.</div>
          )}
        </section>
      </div>
    </div>
  );
}
