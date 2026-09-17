import { FormEvent, useEffect, useState } from "react";
import { jobsApi } from "../features/jobs/api";
import { JobCard } from "../shared/components/JobCard";
import type { Job } from "../shared/types/domain";

export function JobsPage() {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [skill, setSkill] = useState("");
  const [minBudget, setMinBudget] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const load = (query = "?status=OPEN") => {
    setLoading(true);
    jobsApi
      .list(query)
      .then((result) => {
        setJobs(result.content);
        setError("");
      })
      .catch(() => {
        setJobs([]);
        setError("Không thể tải công việc. Hãy kiểm tra backend.");
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, []);

  const search = (event: FormEvent) => {
    event.preventDefault();
    const params = new URLSearchParams({ status: "OPEN" });
    if (skill) params.set("skill", skill);
    if (minBudget) params.set("minBudget", minBudget);
    load(`?${params}`);
  };

  return (
    <div className="page page--soft">
      <section className="container page-heading">
        <span className="eyebrow">Sàn công việc</span>
        <h1>Tìm dự án phù hợp với chuyên môn</h1>
        <p>Khám phá cơ hội minh bạch về ngân sách, kỹ năng và thời hạn.</p>
      </section>
      <div className="container jobs-layout">
        <aside className="filter-card">
          <div className="filter-card__title">
            <span className="material-symbols-outlined">tune</span>
            <h2>Bộ lọc</h2>
          </div>
          <form onSubmit={search} className="form-stack">
            <label>
              Kỹ năng
              <input
                value={skill}
                onChange={(e) => setSkill(e.target.value)}
                placeholder="VD: Spring Boot"
              />
            </label>
            <label>
              Ngân sách tối thiểu
              <input
                value={minBudget}
                onChange={(e) => setMinBudget(e.target.value)}
                type="number"
                min="0"
                placeholder="10.000.000"
              />
            </label>
            <label>
              Trạng thái
              <select disabled>
                <option>Đang mở</option>
              </select>
            </label>
            <button className="button button--primary" type="submit">
              Áp dụng bộ lọc
            </button>
          </form>
        </aside>
        <section className="jobs-results">
          <div className="results-toolbar">
            <div>
              <strong>{jobs.length} công việc</strong>
              <span>Cập nhật gần đây</span>
            </div>
          </div>
          {error && <div className="alert alert--error">{error}</div>}
          {loading ? (
            <div className="loading-card">Đang tìm cơ hội phù hợp...</div>
          ) : (
            <div className="job-list">
              {jobs.map((job) => (
                <JobCard key={job.id} job={job} />
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
