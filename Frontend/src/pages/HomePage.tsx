import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { demoJobs } from "../features/jobs/demoJobs";
import { jobsApi } from "../features/jobs/api";
import { JobCard } from "../shared/components/JobCard";
import type { Job } from "../shared/types/domain";

export function HomePage() {
  const [jobs, setJobs] = useState<Job[]>(demoJobs);

  useEffect(() => {
    // Dữ liệu API thay thế dữ liệu mẫu; dữ liệu mẫu giữ trang chủ hữu ích khi backend tạm lỗi.
    jobsApi
      .list("?status=OPEN&size=3")
      .then((result) => setJobs(result.content))
      .catch(() => {});
  }, []);

  return (
    <>
      <section className="hero">
        <div className="hero__glow hero__glow--one" />
        <div className="hero__glow hero__glow--two" />
        <div className="container hero__grid">
          <div className="hero__copy reveal">
            <span className="pill">
              <span className="pulse" /> Nền tảng freelance dành cho người Việt
            </span>
            <h1>
              Kết nối chuyên gia,
              <br />
              <em>hiện thực ý tưởng</em>
            </h1>
            <p>
              Quy trình rõ ràng, năng lực được xác thực bằng công việc thật và thanh toán an toàn
              qua cơ chế ký quỹ.
            </p>
            <div className="hero__actions">
              <Link className="button button--action button--large" to="/jobs">
                Khám phá công việc <span className="material-symbols-outlined">arrow_forward</span>
              </Link>
              <Link className="button button--outline button--large" to="/post-project">
                Đăng dự án
              </Link>
            </div>
            <div className="trust-row">
              <span>
                <b>12K+</b> chuyên gia
              </span>
              <span>
                <b>98%</b> giao dịch an toàn
              </span>
              <span>
                <b>4.9/5</b> mức hài lòng
              </span>
            </div>
          </div>
          <div className="hero__visual reveal reveal--delay">
            <div className="orbit orbit--outer" />
            <div className="orbit orbit--inner" />
            <div className="hero-card hero-card--main">
              <span className="eyebrow">Dự án nổi bật</span>
              <h3>Ứng dụng tài chính cá nhân</h3>
              <div className="tags">
                <span className="tag">Flutter</span>
                <span className="tag">Fintech</span>
              </div>
              <div className="hero-card__price">25.000.000 ₫</div>
            </div>
            <div className="hero-card hero-card--escrow">
              <span className="material-symbols-outlined">verified_user</span>
              <div>
                <b>Ký quỹ đã bảo vệ</b>
                <small>2,4 tỷ ₫ giao dịch</small>
              </div>
            </div>
            <div className="hero-card hero-card--talent">
              <span className="avatar">MA</span>
              <div>
                <b>Minh Anh</b>
                <small>Top 1% Full-stack</small>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="section" id="process">
        <div className="container">
          <div className="section-heading">
            <div>
              <span className="eyebrow">Cách vận hành</span>
              <h2>Một quy trình, không còn mơ hồ</h2>
            </div>
            <p>Mọi mốc công việc và dòng tiền đều có trạng thái rõ ràng.</p>
          </div>
          <div className="steps">
            {[
              ["edit_square", "01", "Đăng nhu cầu", "Mô tả dự án, ngân sách và kỹ năng cần thiết."],
              [
                "handshake",
                "02",
                "Chọn đúng người",
                "So sánh đề xuất hợp tác và uy tín từ giao dịch thật.",
              ],
              [
                "lock",
                "03",
                "Khóa ngân sách",
                "Tiền demo được tạm giữ an toàn trong tài khoản ký quỹ.",
              ],
              ["task_alt", "04", "Nghiệm thu", "Giải ngân khi kết quả đã được xác nhận."],
            ].map(([icon, no, title, text]) => (
              <article className="step" key={no}>
                <span className="step__number">{no}</span>
                <span className="material-symbols-outlined step__icon">{icon}</span>
                <h3>{title}</h3>
                <p>{text}</p>
              </article>
            ))}
          </div>
        </div>
      </section>

      <section className="section section--tinted">
        <div className="container">
          <div className="section-heading">
            <div>
              <span className="eyebrow">Cơ hội mới</span>
              <h2>Việc tốt đang chờ bạn</h2>
            </div>
            <Link to="/jobs" className="text-link">
              Xem tất cả <span className="material-symbols-outlined">arrow_forward</span>
            </Link>
          </div>
          <div className="job-grid">
            {jobs.map((job, index) => (
              <JobCard key={job.id} job={job} featured={index === 0} />
            ))}
          </div>
        </div>
      </section>

      <section className="section" id="trust">
        <div className="container trust-banner">
          <div>
            <span className="eyebrow eyebrow--light">An tâm cộng tác</span>
            <h2>
              Uy tín không đến từ lời giới thiệu.
              <br />
              Nó đến từ công việc đã hoàn thành.
            </h2>
          </div>
          <div className="trust-banner__points">
            <span>
              <i className="material-symbols-outlined">shield_lock</i>Ký quỹ theo từng dự án
            </span>
            <span>
              <i className="material-symbols-outlined">reviews</i>Đánh giá hai chiều xác thực
            </span>
            <span>
              <i className="material-symbols-outlined">account_tree</i>State machine minh bạch
            </span>
          </div>
        </div>
      </section>
    </>
  );
}
