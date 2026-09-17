import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getCurrentUser } from "../features/auth/session";
import { jobsApi } from "../features/jobs/api";
import { markProposalsSeen, proposalSeenAt, proposalsApi } from "../features/proposals/api";
import type { Job, Proposal } from "../shared/types/domain";

const money = new Intl.NumberFormat("vi-VN", {
  style: "currency",
  currency: "VND",
  maximumFractionDigits: 0,
});

type ReceivedProposalGroup = {
  job: Job;
  proposals: Proposal[];
};

export function ProposalsPage() {
  const user = getCurrentUser();
  const [mine, setMine] = useState<Proposal[]>([]);
  const [received, setReceived] = useState<ReceivedProposalGroup[]>([]);
  const [notice, setNotice] = useState("");
  const [role, setRole] = useState<"received" | "sent">("received");
  const [newReceived, setNewReceived] = useState(0);
  const [newSent, setNewSent] = useState(0);

  // Tải đề xuất đã gửi và đề xuất nhận được cho các dự án do tài khoản hiện tại đăng.
  const load = async () => {
    if (!user) return;
    try {
      const [myProposals, posted] = await Promise.all([proposalsApi.mine(), jobsApi.posted()]);
      const inbox = await Promise.all(
        posted.map(async (job) => ({
          job,
          proposals: (await proposalsApi.forJob(job.id)).content,
        })),
      );
      setMine(myProposals.content);
      setReceived(inbox.filter(({ proposals }) => proposals.length));
      const seen = proposalSeenAt(user.id);
      setNewSent(myProposals.content.filter((item) => Date.parse(item.updatedAt) > seen).length);
      setNewReceived(
        inbox
          .flatMap(({ proposals }) => proposals)
          .filter((item) => Date.parse(item.updatedAt) > seen).length,
      );
      setNotice("");
    } catch {
      setNotice("Không thể tải hộp đề xuất. Hãy kiểm tra phiên đăng nhập.");
    }
  };

  useEffect(() => {
    if (!user) return;
    void load().then(() => markProposalsSeen(user.id));
    const timer = window.setInterval(() => {
      if (!document.hidden) void load();
    }, 4000);
    return () => window.clearInterval(timer);
  }, [user?.id]);
  // Thực hiện thao tác chọn/rút đề xuất rồi đồng bộ lại cả hai vai trò.
  const act = async (action: () => Promise<unknown>, success: string) => {
    try {
      await action();
      setNotice(success);
      await load();
    } catch {
      setNotice("Thao tác không hợp lệ hoặc trạng thái đã thay đổi.");
    }
  };

  if (!user)
    return (
      <div className="page">
        <section className="container empty-state">
          <h1>Hộp đề xuất hợp tác</h1>
          <p>Đăng nhập để xem dự án đã ứng tuyển và đề xuất nhận được.</p>
          <Link className="button button--primary" to="/login">
            Đăng nhập
          </Link>
        </section>
      </div>
    );

  const receivedCount = received.reduce((sum, item) => sum + item.proposals.length, 0);
  return (
    <div className="page page--soft">
      <div className="container dashboard-page">
        <header className="page-heading">
          <span className="eyebrow">Hộp thư nhận việc</span>
          <h1>Quản lý đề xuất hợp tác</h1>
          <p>Chọn đúng vai trò để tập trung vào các đề xuất cần xử lý.</p>
        </header>
        {notice && <div className="alert">{notice}</div>}
        <div className="role-tabs" role="tablist">
          <button
            className={role === "received" ? "role-tab role-tab--active" : "role-tab"}
            onClick={() => {
              setRole("received");
              setNewReceived(0);
              if (user) markProposalsSeen(user.id);
            }}
          >
            Tôi tuyển <span>{receivedCount}</span>
            {newReceived > 0 && <i>{newReceived >= 5 ? "5+" : newReceived}</i>}
          </button>
          <button
            className={role === "sent" ? "role-tab role-tab--active" : "role-tab"}
            onClick={() => {
              setRole("sent");
              setNewSent(0);
              if (user) markProposalsSeen(user.id);
            }}
          >
            Tôi ứng tuyển <span>{mine.length}</span>
            {newSent > 0 && <i>{newSent >= 5 ? "5+" : newSent}</i>}
          </button>
        </div>
        {role === "received" ? (
          <section className="panel dashboard-section">
            <div className="panel-heading">
              <h2>Đề xuất nhận được</h2>
              <span className="pill">{receivedCount}</span>
            </div>
            {received.length ? (
              received.map(({ job, proposals }) => (
                <div className="proposal-group" key={job.id}>
                  <h3>
                    <Link to={`/jobs/${job.id}`}>{job.title}</Link>
                  </h3>
                  {proposals.map((proposal) => (
                    <ProposalRow
                      key={proposal.id}
                      proposal={proposal}
                      action={
                        proposal.status === "PENDING" ? (
                          <button
                            className="button button--action"
                            onClick={() =>
                              void act(
                                () => proposalsApi.accept(proposal.id),
                                "Đã chọn người thực hiện và tạm giữ thù lao trong tài khoản ký quỹ.",
                              )
                            }
                          >
                            Chọn người thực hiện
                          </button>
                        ) : undefined
                      }
                    />
                  ))}
                </div>
              ))
            ) : (
              <div className="empty-inline">Chưa có đề xuất nào gửi tới dự án của bạn.</div>
            )}
          </section>
        ) : (
          <section className="panel dashboard-section">
            <div className="panel-heading">
              <h2>Đề xuất đã gửi</h2>
              <span className="pill">{mine.length}</span>
            </div>
            {mine.length ? (
              mine.map((proposal) => (
                <ProposalRow
                  key={proposal.id}
                  proposal={proposal}
                  action={
                    proposal.status === "PENDING" ? (
                      <button
                        className="button button--ghost"
                        onClick={() =>
                          void act(() => proposalsApi.withdraw(proposal.id), "Đã rút đề xuất.")
                        }
                      >
                        Rút đề xuất
                      </button>
                    ) : undefined
                  }
                />
              ))
            ) : (
              <div className="empty-inline">Bạn chưa gửi đề xuất nào.</div>
            )}
          </section>
        )}
      </div>
    </div>
  );
}

// Trình bày một đề xuất với giá, thời hạn, trạng thái và hành động phù hợp vai trò.
function ProposalRow({ proposal, action }: { proposal: Proposal; action?: React.ReactNode }) {
  return (
    <article className="proposal-row">
      <div>
        <Link to={`/jobs/${proposal.jobId}`}>
          <b>{proposal.jobTitle}</b>
        </Link>
        <small>
          {proposal.freelancerName} · {proposal.deliveryDays} ngày
        </small>
        <p>{proposal.coverLetter}</p>
      </div>
      <div className="proposal-row__action">
        <strong>{money.format(proposal.bidAmount)}</strong>
        <span className={`status status--${proposal.status.toLowerCase()}`}>
          {proposalStatusLabel(proposal.status)}
        </span>
        {action}
      </div>
    </article>
  );
}

// Việt hóa mã trạng thái do API trả về mà không làm thay đổi hợp đồng dữ liệu.
function proposalStatusLabel(status: Proposal["status"]) {
  return {
    PENDING: "Đang chờ",
    ACCEPTED: "Đã chấp nhận",
    REJECTED: "Đã từ chối",
    WITHDRAWN: "Đã rút",
  }[status];
}
