import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { reviewsApi, type Ranking } from "../features/reviews/api";

export function RankingsPage() {
  const [mode, setMode] = useState<"monthly" | "trusted">("monthly");
  const [items, setItems] = useState<Ranking[]>([]);
  const [error, setError] = useState("");
  useEffect(() => {
    // Mỗi tab có tiêu chí xếp hạng riêng nên thay tab sẽ tải lại dữ liệu.
    reviewsApi
      .rankings(mode)
      .then(setItems)
      .catch(() => setError("Không thể tải bảng xếp hạng."));
  }, [mode]);
  return (
    <div className="page page--soft">
      <div className="container dashboard-page">
        <header className="page-heading">
          <span className="eyebrow">Cộng đồng chuyên gia</span>
          <h1>Người làm việc tự do nổi bật</h1>
          <p>Xếp hạng dựa trên dự án hoàn thành và đánh giá có thể truy vết.</p>
        </header>
        <div className="ranking-tabs">
          <button
            className={mode === "monthly" ? "button button--primary" : "button button--outline"}
            onClick={() => setMode("monthly")}
          >
            Top tháng
          </button>
          <button
            className={mode === "trusted" ? "button button--primary" : "button button--outline"}
            onClick={() => setMode("trusted")}
          >
            Uy tín nhất
          </button>
        </div>
        {error && <div className="alert alert--error">{error}</div>}
        <section className="panel ranking-list">
          {items.map((item, index) => (
            <article className="ranking-row" key={item.userId}>
              <strong className="ranking-position">#{index + 1}</strong>
              <span className="avatar">{item.fullName[0]}</span>
              <div>
                <Link to={`/profile/${item.userId}`}>
                  <b>{item.fullName}</b>
                </Link>
                <small>
                  {item.completedJobs} dự án hoàn thành · {item.reviewCount} đánh giá
                </small>
              </div>
              <strong>★ {Number(item.averageRating).toFixed(1)}</strong>
            </article>
          ))}
          {!items.length && !error && <div className="empty-inline">Chưa đủ dữ liệu xếp hạng.</div>}
        </section>
      </div>
    </div>
  );
}
