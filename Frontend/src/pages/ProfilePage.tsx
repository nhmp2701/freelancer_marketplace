import { FormEvent, useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getCurrentUser, updateCurrentUserName } from "../features/auth/session";
import { profileApi, type Profile } from "../features/profile/api";
import { reviewsApi, type Review } from "../features/reviews/api";
import { ApiError } from "../shared/api/client";

export function ProfilePage() {
  const session = getCurrentUser();
  const { userId } = useParams();
  const profileId = userId ? Number(userId) : session?.id;
  const isOwn = !!session && profileId === session.id;
  const [profile, setProfile] = useState<Profile | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [editing, setEditing] = useState(false);
  const [notice, setNotice] = useState("");
  // Tải hồ sơ và đánh giá song song; route không có ID nghĩa là hồ sơ của chính mình.
  const load = useCallback(
    () =>
      profileId
        ? Promise.all([profileApi.get(profileId), reviewsApi.forUser(profileId)])
            .then(([value, items]) => {
              setProfile(value);
              setReviews(items);
            })
            .catch(() => setNotice("Không thể tải hồ sơ."))
        : Promise.resolve(),
    [profileId],
  );
  useEffect(() => {
    void load();
  }, [load]);

  // Lưu hồ sơ rồi đồng bộ tên trên thanh điều hướng của phiên hiện tại.
  const save = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    try {
      const updated = await profileApi.update({
        fullName: String(data.get("fullName")),
        bio: String(data.get("bio")),
        avatarUrl: String(data.get("avatarUrl")),
      });
      setProfile(updated);
      updateCurrentUserName(updated.fullName);
      setEditing(false);
      setNotice("Đã cập nhật hồ sơ.");
    } catch (reason) {
      setNotice(reason instanceof ApiError ? reason.message : "Không thể cập nhật hồ sơ.");
    }
  };
  // Thêm kỹ năng và tải lại hồ sơ để dùng dữ liệu chuẩn hóa từ backend.
  const addSkill = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const form = event.currentTarget;
    const name = String(new FormData(form).get("skill") ?? "").trim();
    if (!name) return;
    try {
      await profileApi.addSkill(name);
      form.reset();
      await load();
    } catch {
      setNotice("Không thể thêm kỹ năng.");
    }
  };
  // Xóa kỹ năng; backend tiếp tục kiểm tra quyền sở hữu hồ sơ.
  const removeSkill = async (name: string) => {
    try {
      await profileApi.removeSkill(name);
      await load();
    } catch {
      setNotice("Không thể xóa kỹ năng.");
    }
  };

  if (!profileId)
    return (
      <div className="page">
        <section className="container empty-state">
          <h1>Hồ sơ năng lực</h1>
          <p>Đăng nhập để cập nhật hồ sơ và kỹ năng.</p>
          <Link className="button button--primary" to="/login">
            Đăng nhập
          </Link>
        </section>
      </div>
    );
  if (!profile)
    return (
      <div className="page">
        <section className="container empty-state">
          <h1>{notice || "Đang tải hồ sơ..."}</h1>
        </section>
      </div>
    );
  return (
    <div className="page page--soft">
      <div className="container profile-layout">
        <aside className="panel profile-card">
          <div className="profile-card__cover" />
          <span className="avatar avatar--large">{profile.fullName[0]}</span>
          <h1>{profile.fullName}</h1>
          <p className="profile-role">{profile.email}</p>
          <div className="reputation">
            <span className="material-symbols-outlined">workspace_premium</span>
            <div>
              <b>{Number(profile.reputationScore ?? 0).toFixed(1)} / 5</b>
              <small>{reviews.length} đánh giá đã xác thực</small>
            </div>
          </div>
          {isOwn && (
            <button
              className="button button--outline"
              onClick={() => setEditing((value) => !value)}
            >
              Chỉnh sửa hồ sơ
            </button>
          )}
        </aside>
        <div className="profile-content">
          {notice && <div className="alert">{notice}</div>}
          {editing && (
            <form className="panel form-stack" onSubmit={save}>
              <div className="panel-heading">
                <h2>Thông tin cá nhân</h2>
              </div>
              <label>
                Họ tên
                <input name="fullName" required maxLength={255} defaultValue={profile.fullName} />
              </label>
              <label>
                Ảnh đại diện (URL)
                <input
                  name="avatarUrl"
                  type="url"
                  maxLength={500}
                  defaultValue={profile.avatarUrl}
                />
              </label>
              <label>
                Giới thiệu
                <textarea name="bio" rows={6} maxLength={5000} defaultValue={profile.bio} />
              </label>
              <div className="form-actions">
                <button
                  type="button"
                  className="button button--ghost"
                  onClick={() => setEditing(false)}
                >
                  Hủy
                </button>
                <button className="button button--primary">Lưu thay đổi</button>
              </div>
            </form>
          )}
          <section className="panel">
            <div className="panel-heading">
              <div>
                <span className="eyebrow">Giới thiệu</span>
                <h2>Năng lực chuyên môn</h2>
              </div>
              <span className="status status--open">{profile.status}</span>
            </div>
            <p className="profile-bio">{profile.bio || "Chưa có phần giới thiệu."}</p>
            <div className="skill-picker">
              {profile.skills.length ? (
                profile.skills.map((skill) =>
                  isOwn ? (
                    <button
                      type="button"
                      className="tag tag--removable"
                      key={skill}
                      onClick={() => void removeSkill(skill)}
                    >
                      {skill} ×
                    </button>
                  ) : (
                    <span className="tag" key={skill}>
                      {skill}
                    </span>
                  ),
                )
              ) : (
                <span>Chưa có kỹ năng.</span>
              )}
            </div>
            {isOwn && (
              <form className="inline-form" onSubmit={addSkill}>
                <input name="skill" required maxLength={100} placeholder="Thêm kỹ năng" />
                <button className="button button--outline">Thêm</button>
              </form>
            )}
          </section>
          <section className="panel">
            <div className="panel-heading">
              <h2>Đánh giá từ đối tác</h2>
              <span className="pill">{reviews.length}</span>
            </div>
            {reviews.map((review) => (
              <article className="review-row" key={review.id}>
                <b>
                  {"★".repeat(review.rating)}
                  {"☆".repeat(5 - review.rating)}
                </b>
                <p>{review.comment || "Không có nhận xét."}</p>
                <small>
                  {review.reviewerName} · {review.jobTitle}
                </small>
              </article>
            ))}
            {!reviews.length && <div className="empty-inline">Chưa có đánh giá.</div>}
          </section>
        </div>
      </div>
    </div>
  );
}
