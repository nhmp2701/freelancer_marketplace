import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { jobsApi, suggestedSkills } from "../features/jobs/api";
import { ApiError } from "../shared/api/client";
import type { Skill } from "../shared/types/domain";

export function PostProjectPage() {
  const navigate = useNavigate();
  const [skills, setSkills] = useState<Skill[]>(suggestedSkills);
  const [selected, setSelected] = useState<number[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [customSkill, setCustomSkill] = useState("");

  // Thu thập dữ liệu đã được trình duyệt kiểm tra rồi tạo dự án qua API.
  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    setLoading(true);
    setError("");
    try {
      const job = await jobsApi.create({
        title: data.get("title"),
        description: data.get("description"),
        budget: Number(data.get("budget")),
        deadline: String(data.get("deadline")),
        skills: skills.filter(({ id }) => selected.includes(id)).map(({ name }) => name),
      });
      navigate(`/jobs/${job.id}`);
    } catch (reason) {
      setError(
        reason instanceof ApiError ? reason.message : "Hãy đăng nhập và kiểm tra kết nối backend.",
      );
    } finally {
      setLoading(false);
    }
  };

  const toggle = (id: number) =>
    setSelected((current) =>
      current.includes(id) ? current.filter((item) => item !== id) : [...current, id],
    );
  // Thêm kỹ năng tùy chỉnh nhưng không tạo phần tử trùng trong danh sách đã chọn.
  const addSkill = () => {
    const name = customSkill.trim().replace(/\s+/g, " ");
    if (!name) return;
    const existing = skills.find(
      (skill) => skill.name.toLocaleLowerCase() === name.toLocaleLowerCase(),
    );
    const skill = existing ?? {
      id: Math.max(0, ...skills.map(({ id }) => id)) + 1,
      name,
    };
    if (!existing) setSkills((current) => [...current, skill]);
    setSelected((current) => (current.includes(skill.id) ? current : [...current, skill.id]));
    setCustomSkill("");
  };

  return (
    <div className="page page--soft">
      <div className="container form-page">
        <header>
          <span className="eyebrow">Dành cho khách hàng</span>
          <h1>Biến ý tưởng thành một brief rõ ràng</h1>
          <p>Thông tin càng cụ thể, đề xuất hợp tác bạn nhận được càng phù hợp.</p>
        </header>
        <form className="panel project-form" onSubmit={submit}>
          <div className="form-section">
            <span className="form-section__number">01</span>
            <div>
              <h2>Tổng quan dự án</h2>
              <p>Đặt tiêu đề ngắn gọn và mô tả kết quả bạn cần.</p>
            </div>
          </div>
          <label>
            Tiêu đề dự án
            <input
              name="title"
              required
              maxLength={200}
              placeholder="VD: Xây dựng ứng dụng quản lý chi tiêu"
            />
          </label>
          <label>
            Mô tả chi tiết
            <textarea
              name="description"
              required
              rows={8}
              maxLength={5000}
              placeholder="Bối cảnh, phạm vi, kết quả bàn giao và tiêu chí nghiệm thu..."
            />
          </label>
          <div className="form-section form-section--spaced">
            <span className="form-section__number">02</span>
            <div>
              <h2>Ngân sách và thời gian</h2>
              <p>Ngân sách được thỏa thuận khi bạn chọn người thực hiện.</p>
            </div>
          </div>
          <div className="form-grid">
            <label>
              Ngân sách (VNĐ)
              <input name="budget" type="number" min="10000" required placeholder="20000000" />
            </label>
            <label>
              Hạn hoàn thành
              <input name="deadline" type="datetime-local" required />
            </label>
          </div>
          <label>
            Kỹ năng cần thiết
            <div className="skill-picker">
              {skills.map((skill) => (
                <button
                  type="button"
                  className={selected.includes(skill.id) ? "tag tag--selected" : "tag"}
                  key={skill.id}
                  onClick={() => toggle(skill.id)}
                >
                  {skill.name}
                </button>
              ))}
            </div>
          </label>
          <div className="inline-form">
            <input
              value={customSkill}
              onChange={(event) => setCustomSkill(event.target.value)}
              maxLength={100}
              placeholder="Không thấy kỹ năng? Nhập kỹ năng mới"
            />
            <button type="button" className="button button--outline" onClick={addSkill}>
              Thêm kỹ năng
            </button>
          </div>
          {error && <div className="alert alert--error">{error}</div>}
          <div className="form-actions">
            <button type="button" className="button button--ghost" onClick={() => navigate(-1)}>
              Hủy
            </button>
            <button className="button button--action button--large" disabled={loading}>
              {loading ? "Đang đăng..." : "Đăng dự án"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
