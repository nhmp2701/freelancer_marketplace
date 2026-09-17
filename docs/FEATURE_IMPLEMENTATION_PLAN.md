# Kế hoạch hoàn thiện Freelancer Marketplace

> Ngày lập: 2026-09-17
> Phạm vi: các capability người dùng yêu cầu, triển khai theo vertical slice end-to-end và dùng API/dữ liệu thật.

## 1. Mục tiêu và nguyên tắc

Luồng đích của MVP:

```text
Client đăng job -> Freelancer gửi proposal -> Client nhận/chọn proposal
-> Hai bên trao đổi -> Freelancer bàn giao -> Client nghiệm thu
-> Thanh toán demo có ledger -> Hai bên đánh giá -> Cập nhật uy tín/xếp hạng
```

Nguyên tắc:

- Một tài khoản có thể vừa đăng job vừa nhận job; quyền được quyết định theo quan hệ với từng job.
- Tin nhắn bản đầu dùng HTTP polling, chưa thêm WebSocket hoặc attachment.
- Tiền là tiền demo nhưng vẫn dùng transaction append-only, idempotency và khóa bản ghi; không cộng số dư trực tiếp từ frontend.
- Điểm uy tín được tính lại từ review hợp lệ, không nhận tổng điểm do client gửi.
- Không thêm dependency nếu Spring/React và thư viện hiện có đã đáp ứng.
- Mọi mutation phải kiểm tra authentication, ownership/participant và trạng thái nghiệp vụ.

## 2. Ma trận capability

| ID | Capability | Backend | Frontend | Tiêu chí chấp nhận |
|---|---|---|---|---|
| F1 | Phân biệt job của tôi/người khác | Dùng `clientId`, `freelancerId` hiện có | Badge, CTA và tab job cá nhân | Chủ job không thấy form proposal; job đã nhận/đã đăng tách rõ |
| F2 | Hộp thư nhận job/proposal | API proposal hiện có | Trang proposal gửi/nhận, accept/withdraw | Client xem proposal theo job và chọn freelancer; freelancer xem proposal của mình |
| F3 | Chỉnh profile/kỹ năng | API profile/skill hiện có | Form edit, thêm/xóa skill | Refresh hiển thị dữ liệu đã lưu; validation/lỗi rõ ràng |
| F4 | Kỹ năng đăng job linh hoạt | Skill resolver hiện có | Danh mục gợi ý mở rộng + input thêm tùy ý | Chọn gợi ý hoặc thêm skill mới, không trùng/rỗng |
| F5 | Quản lý dự án | Transition submit/accept/reject + history | Dashboard job đã đăng/đã nhận, tiến độ và bàn giao | Chỉ freelancer được gán được submit; chỉ client sở hữu được nghiệm thu/yêu cầu sửa |
| F6 | Tin nhắn | Conversation theo job, participant-only; message pagination/read | Danh sách hội thoại + polling + gửi tin | Chỉ hai đối tác của job truy cập; tin nhắn được lưu và đánh dấu đã đọc |
| F7 | Đánh giá profile | Một review/người/job sau completed | Form review + danh sách review trên profile | Không tự đánh giá, không đánh giá trùng; điểm 1..5; reputation truy vết được |
| F8 | Top freelancer tháng/uy tín | Aggregate completed jobs + review | Section bảng xếp hạng | Xếp hạng có số job, số review, điểm trung bình; tie-break ổn định |
| F9 | Ví/nạp tiền demo | Ledger append-only + idempotency | Số dư, form nạp, lịch sử | Retry cùng idempotency key không nhân đôi; amount hợp lệ; lịch sử đối soát được |

## 3. Thiết kế chi tiết theo vertical slice

### Slice A — Profile, kỹ năng và nhận diện job

- Mở form chỉnh `fullName`, `bio`, `avatarUrl` ngay tại trang profile.
- Cho thêm/xóa skill qua API hiện có và tải lại profile sau mutation.
- Danh mục gợi ý đăng job gồm web, mobile, backend, data, design, marketing, content và business.
- Input “Thêm kỹ năng” chuẩn hóa khoảng trắng, chống trùng không phân biệt hoa thường.
- `JobCard` nhận user hiện tại để hiện `Bạn đăng`, `Bạn đang thực hiện` hoặc `Job công khai`.
- Trang chi tiết ẩn form proposal với chủ job và chuyển CTA theo trạng thái/quan hệ.

Kiểm chứng: backend profile tests, `npm run build`, smoke login -> edit profile -> post job với custom skill.

### Slice B — Hộp thư proposal

- Trang `/proposals` có hai vùng: proposal đã gửi và proposal nhận theo từng job đã đăng.
- Client accept proposal `PENDING`; freelancer withdraw proposal `PENDING`.
- Sau accept, dữ liệu job đổi sang `IN_PROGRESS` và xuất hiện trong dashboard dự án.
- “Hộp thư nhận job” dùng số proposal nhận được và trạng thái thật, không dùng notification giả.

Kiểm chứng: proposal service tests, smoke hai tài khoản gửi -> nhận -> accept.

### Slice C — Dashboard dự án và lifecycle

- Thêm endpoint action thay vì cho client sửa `status` tùy ý:
  - `POST /jobs/{id}/submit` với URL/ghi chú bàn giao.
  - `POST /jobs/{id}/accept-submission`.
  - `POST /jobs/{id}/request-changes` với lý do.
- Lưu dữ liệu bàn giao và `JobStatusHistory` cho mọi transition.
- Dashboard `/projects` chia “Tôi thuê” và “Tôi làm”, hiện timeline trạng thái và action đúng quyền.
- Nghiệm thu chuyển `SUBMITTED -> COMPLETED`; yêu cầu sửa chuyển `SUBMITTED -> IN_PROGRESS`.

Kiểm chứng: unit test authorization/trạng thái, test transition/history, frontend build, smoke submit -> accept.

### Slice D — Tin nhắn

- Conversation duy nhất theo job, chỉ tạo khi job đã có freelancer.
- Participant là `job.client` và `job.freelancer`; mọi query/mutation kiểm tra participant.
- Message gồm sender, body, createdAt; read timestamp theo participant/conversation.
- API danh sách conversation, đọc message phân trang, gửi message, mark-read.
- UI polling định kỳ khi đang mở hội thoại; dừng polling khi unmount/tab ẩn.

Kiểm chứng: test người ngoài bị 403, hai participant gửi/đọc được, pagination ổn định, frontend build.

### Slice E — Review và bảng xếp hạng

- Review chỉ tạo cho job `COMPLETED`, reviewer phải là một phía và reviewee là phía còn lại.
- Unique `(job_id, reviewer_id)`; rating từ 1 đến 5; comment có giới hạn độ dài.
- Sau ghi review, tính lại `users.reputation_score = AVG(rating)` trong cùng transaction.
- Profile trả review summary và danh sách review qua endpoint riêng.
- Rankings:
  - `monthly`: ưu tiên số job completed trong tháng, rồi average rating, review count, user id.
  - `trusted`: ưu tiên average rating, review count, completed jobs; yêu cầu ít nhất một review.

Kiểm chứng: constraint/test duplicate/authorization, aggregate tests, UI profile/ranking build.

### Slice F — Ledger và nạp tiền demo

- Ledger entry append-only chứa user, type, amount, balanceAfter, lockedAfter, reference/job, idempotency key, timestamp.
- Top-up khóa wallet, validate amount dương và giới hạn demo, dùng idempotency key unique theo user.
- API `GET /wallet`, `GET /wallet/transactions`, `POST /wallet/top-up`.
- Không triển khai payment provider thật; chưa escrow/release cho tới khi lifecycle và policy funding được xác nhận.

Kiểm chứng: test retry idempotent, concurrent/locking path, invalid amount, lịch sử; UI wallet build/smoke.

## 4. Migration dự kiến

- `V4`: submission fields và lý do yêu cầu sửa trên jobs (nếu schema chưa có).
- `V5`: conversations/messages/read state và unique theo job.
- `V6`: reviews và unique review/job/reviewer.
- `V7`: wallet transactions/ledger và idempotency constraint.

Migration chỉ đi tới, không sửa migration đã chạy. Foreign key và unique constraint là lớp bảo vệ cuối cùng cho invariant.

## 5. Bảo mật và trường hợp lỗi phải review

- IDOR: mọi endpoint `/me`, proposal, project, conversation, review, wallet tra user từ JWT chứ không nhận user id từ request.
- Job transition: lock job trước khi đổi trạng thái; reject transition không hợp lệ bằng 409.
- Message: không cho user ngoài participant suy ra nội dung hoặc sự tồn tại của conversation.
- Review: không cho review trước hoàn thành, review chính mình hoặc sửa điểm tổng hợp trực tiếp.
- Money: `BigDecimal`, amount dương, transaction + row lock, idempotency key, ledger không update/delete.
- XSS: React render text; URL chỉ lưu/hiển thị dạng link an toàn, không render HTML từ user.

## 6. Definition of Done

Một slice chỉ hoàn thành khi:

- hành vi quan sát được chạy end-to-end;
- test service/controller quan trọng pass;
- frontend typecheck/build pass;
- authorization, validation, lỗi và transaction đã được xét;
- CodeGraph blast radius và `git diff` không có thay đổi ngoài phạm vi;
- `git diff --check` pass;
- roadmap, project status và checklist review được cập nhật.

## 7. Ngoài phạm vi hiện tại

- WebSocket, chat attachment, gọi video.
- Payment provider hoặc tiền thật, rút tiền thật, multi-currency.
- Dispute/admin production, file upload và recommendation AI.
- Deploy production hoặc thay đổi secret/credential.
