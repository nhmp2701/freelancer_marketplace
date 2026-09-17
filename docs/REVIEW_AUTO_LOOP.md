# Checklist review và auto-loop

> File làm việc để người dùng review và Codex tự tiếp tục.
> Trạng thái: `[ ]` chưa làm, `[~]` đang làm, `[x]` đã đạt Definition of Done, `[!]` bị chặn.

## Cách review

- Review theo từng slice; ghi nhận xét ngay dưới mục tương ứng.
- Một checkbox chỉ chuyển sang `[x]` khi code, test, build và tài liệu cùng khớp.
- Codex chọn mục `[ ]` đầu tiên có dependency đã `[x]`, triển khai, verify, self-review, record rồi lặp lại.
- Nếu dừng vì quyết định sản phẩm/bảo mật/tiền, giữ mục `[!]` cùng đúng một câu hỏi cần trả lời.

## Baseline

- [x] Dùng CodeGraph khảo sát kiến trúc và blast radius ban đầu.
- [x] Ghi nhận và bảo toàn thay đổi có sẵn của người dùng trong `.env.example`, `.gitignore`, `application.yml`, `README.md`.
- [x] Lập kế hoạch chi tiết tại `docs/FEATURE_IMPLEMENTATION_PLAN.md`.
- [x] Chạy baseline backend tests và frontend build trước thay đổi nghiệp vụ.

## Slice A — Profile, kỹ năng, nhận diện job

- [x] Form chỉnh profile gọi `PUT /api/v1/users/me`.
- [x] Thêm/xóa kỹ năng gọi API thật và refresh profile.
- [x] Mở rộng gợi ý kỹ năng và thêm custom skill khi đăng job.
- [x] Badge/CTA phân biệt job tự đăng, job đang nhận và job người khác.
- [x] Test/build/smoke và CodeGraph review đạt.

Ghi chú review của người dùng:

> _Chưa có._

## Slice B — Hộp thư proposal/nhận job

- [x] Trang proposal đã gửi.
- [x] Hộp proposal nhận theo job đã đăng.
- [x] Accept/withdraw đúng quyền và trạng thái.
- [x] Điều hướng và trạng thái rỗng/lỗi/loading hoàn chỉnh.
- [x] Test/build/smoke và CodeGraph review đạt.

Ghi chú review của người dùng:

> _Chưa có._

## Slice C — Quản lý dự án, bàn giao và nghiệm thu

- [x] Migration dữ liệu bàn giao.
- [x] API submit/accept/request-changes có authorization và history.
- [x] Dashboard “Tôi thuê”/“Tôi làm” theo dữ liệu thật.
- [x] UI bàn giao, yêu cầu sửa và nghiệm thu.
- [x] Test transaction/transition/build/smoke và CodeGraph review đạt.

Ghi chú review của người dùng:

> _Chưa có._

## Slice D — Tin nhắn

- [x] Migration conversation/message/read state.
- [x] API participant-only, pagination, send, mark-read.
- [x] UI danh sách hội thoại và polling hội thoại đang mở.
- [x] Unread/empty/error/loading state.
- [x] Test authorization/build/smoke và CodeGraph review đạt.

Ghi chú review của người dùng:

> _Chưa có._

## Slice E — Đánh giá và freelancer nổi bật

- [x] Migration review và uniqueness.
- [x] API tạo/xem review và cập nhật reputation từ aggregate.
- [x] UI đánh giá sau completed và review trên profile.
- [x] API/UI top tháng và freelancer uy tín.
- [x] Test aggregate/authorization/build/smoke và CodeGraph review đạt.

Ghi chú review của người dùng:

> _Chưa có._

## Slice F — Ví và nạp tiền demo

- [x] Migration ledger/transaction/idempotency.
- [x] API wallet/history/top-up có row lock và idempotency.
- [x] UI nạp demo và lịch sử dùng API thật.
- [x] Test money/invariant/concurrency/build/smoke và CodeGraph review đạt.

Ghi chú review của người dùng:

> _Chưa có._

## Cổng hoàn tất toàn bộ

- [x] `Backend/.\mvnw.cmd test` pass (34 tests).
- [x] `Frontend/npm run build` pass.
- [x] Smoke hai tài khoản đi hết job -> proposal -> project -> message -> complete -> review.
- [x] Smoke top-up retry không nhân đôi giao dịch (1 ledger entry, balance 1.000.000).
- [x] `git diff --check` pass và không có secret/artifact/thay đổi ngoài phạm vi.
- [x] `docs/PROJECT_STATUS.md` và `docs/DEVELOPMENT_ROADMAP.md` phản ánh đúng capability thực tế.
