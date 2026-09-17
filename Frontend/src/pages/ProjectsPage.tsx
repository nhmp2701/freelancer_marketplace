import { FormEvent, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { getCurrentUser } from "../features/auth/session";
import { jobsApi } from "../features/jobs/api";
import { messagesApi } from "../features/messages/api";
import { reviewsApi } from "../features/reviews/api";
import { statusLabel } from "../shared/components/JobCard";
import type { Job } from "../shared/types/domain";

export function ProjectsPage() {
  const user = getCurrentUser();
  const navigate = useNavigate();
  const [posted, setPosted] = useState<Job[]>([]);
  const [accepted, setAccepted] = useState<Job[]>([]);
  const [notice, setNotice] = useState("");
  const [role, setRole] = useState<"client" | "freelancer">("client");
  const [reviewedJobs, setReviewedJobs] = useState<Set<number>>(new Set());
  // Tải dữ liệu của cả hai vai trò và danh sách đánh giá đã gửi trong một lượt.
  const load = async () => {
    if (!user) return;
    try {
      const [a, b, reviews] = await Promise.all([
        jobsApi.posted(),
        jobsApi.accepted(),
        reviewsApi.mine(),
      ]);
      setPosted(a);
      setAccepted(b);
      setReviewedJobs(new Set(reviews.map((review) => review.jobId)));
    } catch {
      setNotice("Không thể tải danh sách dự án.");
    }
  };
  useEffect(() => {
    void load();
    const timer = window.setInterval(() => {
      if (!document.hidden) void load();
    }, 4000);
    return () => window.clearInterval(timer);
  }, [user?.id]);
  // Bao bọc các thao tác dự án để thống nhất thông báo lỗi và tự làm mới giao diện.
  const act = async (action: () => Promise<unknown>, message: string) => {
    try {
      await action();
      setNotice(message);
      await load();
    } catch {
      setNotice("Thao tác không hợp lệ, đã thực hiện trước đó hoặc trạng thái đã thay đổi.");
    }
  };
  // Mở hoặc lấy hội thoại gắn với dự án trước khi chuyển sang trang tin nhắn.
  const chat = async (jobId: number) => {
    try {
      await messagesApi.open(jobId);
      navigate("/messages");
    } catch {
      setNotice("Chỉ đối tác của dự án mới có thể mở hội thoại.");
    }
  };
  if (!user)
    return (
      <div className="page">
        <section className="container empty-state">
          <h1>Dự án của tôi</h1>
          <p>Đăng nhập để theo dõi các dự án của hai vai trò.</p>
          <Link className="button button--primary" to="/login">
            Đăng nhập
          </Link>
        </section>
      </div>
    );
  return (
    <div className="page page--soft">
      <div className="container dashboard-page">
        <header className="page-heading">
          <span className="eyebrow">Không gian hợp tác</span>
          <h1>Dự án của tôi</h1>
          <p>Chọn vai trò để quản lý đúng công việc cần xử lý.</p>
        </header>
        {notice && <div className="alert">{notice}</div>}
        <div className="role-tabs" role="tablist">
          <button
            className={role === "client" ? "role-tab role-tab--active" : "role-tab"}
            onClick={() => setRole("client")}
          >
            Tôi thuê <span>{posted.length}</span>
          </button>
          <button
            className={role === "freelancer" ? "role-tab role-tab--active" : "role-tab"}
            onClick={() => setRole("freelancer")}
          >
            Tôi làm <span>{accepted.length}</span>
          </button>
        </div>
        {role === "client" ? (
          <ProjectList
            title="Dự án tôi đăng"
            jobs={posted}
            empty="Bạn chưa đăng dự án nào."
            owner
            reviewedJobs={reviewedJobs}
            onAct={act}
            onChat={chat}
          />
        ) : (
          <ProjectList
            title="Dự án tôi nhận"
            jobs={accepted}
            empty="Bạn chưa nhận dự án nào."
            reviewedJobs={reviewedJobs}
            onAct={act}
            onChat={chat}
          />
        )}
      </div>
    </div>
  );
}

function ProjectList({
  title,
  jobs,
  empty,
  owner = false,
  reviewedJobs,
  onAct,
  onChat,
}: {
  title: string;
  jobs: Job[];
  empty: string;
  owner?: boolean;
  reviewedJobs: Set<number>;
  onAct: Action;
  onChat: (jobId: number) => Promise<void>;
}) {
  return (
    <section>
      <div className="panel-heading">
        <h2>{title}</h2>
        <span className="pill">{jobs.length}</span>
      </div>
      <div className="project-list">
        {jobs.map((job) => (
          <ProjectRow
            key={job.id}
            job={job}
            owner={owner}
            reviewed={reviewedJobs.has(job.id)}
            onAct={onAct}
            onChat={onChat}
          />
        ))}
      </div>
      {!jobs.length && <div className="empty-inline">{empty}</div>}
    </section>
  );
}

type Action = (action: () => Promise<unknown>, message: string) => Promise<void>;
function ProjectRow({
  job,
  owner,
  reviewed,
  onAct,
  onChat,
}: {
  job: Job;
  owner: boolean;
  reviewed: boolean;
  onAct: Action;
  onChat: (jobId: number) => Promise<void>;
}) {
  // Gửi sản phẩm để khách hàng nghiệm thu; backend sẽ khóa trạng thái chống gửi lặp.
  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    void onAct(
      () =>
        jobsApi.submit(job.id, {
          submissionUrl: data.get("submissionUrl"),
          submissionNote: data.get("submissionNote"),
        }),
      "Đã gửi bàn giao để khách hàng nghiệm thu.",
    );
  };
  // Khách hàng trả dự án về trạng thái đang thực hiện kèm lý do chỉnh sửa.
  const changes = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const reason = String(new FormData(event.currentTarget).get("reason"));
    void onAct(() => jobsApi.requestChanges(job.id, reason), "Đã gửi yêu cầu chỉnh sửa.");
  };
  // Mỗi tài khoản chỉ được đánh giá đối tác một lần cho mỗi dự án hoàn thành.
  const review = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);
    void onAct(
      () => reviewsApi.create(job.id, Number(data.get("rating")), String(data.get("comment"))),
      "Đã gửi đánh giá cho đối tác.",
    );
    form.reset();
  };
  // Người thực hiện cập nhật phần trăm và ghi chú để khách hàng theo dõi gần thời gian thực.
  const progress = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);
    void onAct(
      () => jobsApi.updateProgress(job.id, Number(data.get("progress")), String(data.get("note"))),
      "Đã cập nhật tiến độ dự án.",
    );
    form.reset();
  };
  return (
    <article className="panel project-row">
      <div className="project-row__heading">
        <div>
          <span className={`status status--${job.status.toLowerCase()}`}>
            {statusLabel(job.status)}
          </span>
          <h3>
            <Link to={`/jobs/${job.id}`}>{job.title}</Link>
          </h3>
          <p>
            Đối tác: {owner ? job.freelancerName || "Chưa chọn người thực hiện" : job.clientName}
          </p>
        </div>
        {job.freelancerId && (
          <button className="button button--outline" onClick={() => void onChat(job.id)}>
            Nhắn tin
          </button>
        )}
      </div>
      {job.freelancerId && (
        <div className="project-progress">
          <div>
            <b>Tiến độ dự án</b>
            <strong className="progress-number">{job.progress ?? 0}%</strong>
          </div>
          {job.progressNote && (
            <p>
              {job.progressNote}
              <small>
                {job.progressUpdatedAt
                  ? ` · ${new Date(job.progressUpdatedAt).toLocaleString("vi-VN")}`
                  : ""}
              </small>
            </p>
          )}
        </div>
      )}
      {job.revisionNote && <div className="alert">Yêu cầu chỉnh sửa: {job.revisionNote}</div>}
      {job.submissionNote && (
        <div className="delivery">
          <b>Nội dung bàn giao</b>
          <p>{job.submissionNote}</p>
          {job.submissionUrl && (
            <a
              className="button button--primary delivery-link"
              href={job.submissionUrl}
              target="_blank"
              rel="noreferrer"
            >
              <span className="material-symbols-outlined">open_in_new</span>Mở sản phẩm bàn giao
            </a>
          )}
        </div>
      )}
      {!owner && job.status === "IN_PROGRESS" && (
        <>
          <form className="progress-form" onSubmit={progress}>
            <label>
              Phần trăm
              <input
                name="progress"
                type="number"
                min="0"
                max="99"
                defaultValue={job.progress ?? 0}
                required
              />
            </label>
            <label>
              Cập nhật mới
              <input
                name="note"
                required
                maxLength={500}
                placeholder="Ví dụ: Đã hoàn thành bản thiết kế"
              />
            </label>
            <button className="button button--primary">Cập nhật</button>
          </form>
          <form className="form-stack delivery-form" onSubmit={submit}>
            <h4>Bàn giao sản phẩm</h4>
            <label>
              Liên kết sản phẩm
              <input name="submissionUrl" type="url" maxLength={1000} placeholder="https://..." />
            </label>
            <label>
              Ghi chú bàn giao
              <textarea name="submissionNote" required maxLength={5000} rows={3} />
            </label>
            <button className="button button--action">Gửi bàn giao</button>
          </form>
        </>
      )}
      {owner && job.status === "SUBMITTED" && (
        <div className="review-actions">
          <button
            className="button button--action"
            onClick={() =>
              void onAct(
                () => jobsApi.acceptSubmission(job.id),
                "Đã nghiệm thu và giải ngân thù lao cho người thực hiện.",
              )
            }
          >
            Nghiệm thu & giải ngân
          </button>
          <form className="inline-form" onSubmit={changes}>
            <input name="reason" required maxLength={500} placeholder="Lý do cần chỉnh sửa" />
            <button className="button button--outline">Yêu cầu sửa</button>
          </form>
        </div>
      )}
      {job.status === "COMPLETED" && !owner && (
        <div className="payment-complete">
          <span className="material-symbols-outlined">payments</span>
          Thù lao đã được giải ngân vào ví của bạn
          <Link to="/wallet">Xem giao dịch</Link>
        </div>
      )}
      {job.status === "COMPLETED" &&
        (reviewed ? (
          <div className="review-complete">
            <span className="material-symbols-outlined">verified</span> Bạn đã đánh giá dự án này
          </div>
        ) : (
          <form className="review-form" onSubmit={review}>
            <label>
              Đánh giá đối tác
              <select name="rating" required defaultValue="5">
                <option value="5">5 - Xuất sắc</option>
                <option value="4">4 - Tốt</option>
                <option value="3">3 - Đạt</option>
                <option value="2">2 - Cần cải thiện</option>
                <option value="1">1 - Không đạt</option>
              </select>
            </label>
            <label>
              Nhận xét
              <textarea name="comment" maxLength={2000} rows={2} />
            </label>
            <button className="button button--primary">Gửi đánh giá</button>
          </form>
        ))}
    </article>
  );
}
