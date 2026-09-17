# Hiện trạng dự án Freelancer Marketplace

> Cập nhật: 2026-09-17
> Nguồn: mã nguồn hiện tại, CodeGraph, backend tests, frontend build, Flyway/PostgreSQL và smoke API hai tài khoản.

## Tóm tắt

Dự án hiện có một luồng marketplace MVP chạy end-to-end:

```text
Đăng ký -> cập nhật profile/kỹ năng -> đăng job -> gửi/nhận proposal
-> chọn freelancer -> nhắn tin -> bàn giao -> nghiệm thu
-> đánh giá -> cập nhật uy tín/xếp hạng
```

Ví demo đã kết nối API thật với ledger append-only cho top-up, khóa wallet và idempotency. Đây vẫn là tiền demo; chưa có provider, escrow/release/refund/withdraw thật.

## Capability đã triển khai

| Capability | Trạng thái |
|---|---|
| Auth JWT, register/login/logout | Hoàn thành |
| Public jobs, filter, detail, create job | Hoàn thành |
| Phân biệt job tự đăng/job đang làm/job người khác | Hoàn thành |
| Profile edit và thêm/xóa kỹ năng | Hoàn thành |
| Skill picker mở rộng và custom skill | Hoàn thành |
| Proposal đã gửi và hộp proposal nhận được | Hoàn thành |
| Client chọn freelancer, job chuyển `IN_PROGRESS` | Hoàn thành |
| Dashboard “Tôi thuê”/“Tôi làm” | Hoàn thành |
| Freelancer bàn giao, client yêu cầu sửa/nghiệm thu | Hoàn thành |
| Conversation theo job, HTTP polling, read state | Hoàn thành bản MVP, chưa attachment/WebSocket |
| Badge tổng tin chưa đọc desktop/mobile và UI chat trực quan | Hoàn thành bằng polling nền, chưa WebSocket/push hệ điều hành |
| Tiến độ dự án theo phần trăm/ghi chú và tự đồng bộ | Hoàn thành; chỉ freelancer được gán cập nhật khi đang thực hiện |
| Project/Proposal tách quản lý theo vai trò | Hoàn thành bằng tab trong từng trang |
| Proposal update badge | Hoàn thành trên desktop/mobile bằng polling nền và mốc đã xem trong trình duyệt |
| Escrow giá bid và giải ngân khi nghiệm thu | Hoàn thành bản ví nội bộ: row lock, idempotency và ledger hai phía |
| Xuất sao kê | Hoàn thành CSV UTF-8 từ sổ giao dịch của tài khoản |
| Chuẩn code | Java dùng Google Java Format; frontend dùng Prettier và có format check |
| Review một lần mỗi phía sau `COMPLETED` | Hoàn thành |
| Reputation từ trung bình review có thể truy vết | Hoàn thành |
| Top freelancer tháng và freelancer uy tín | Hoàn thành |
| Wallet balance/history/top-up demo | Hoàn thành bản demo với ledger và idempotency |

## Backend

- Spring Boot 4.1.1, Java 21, Spring Security JWT, Spring Data JPA, PostgreSQL 15.
- Flyway schema đến `V8`:
  - `V4`: dữ liệu bàn giao và yêu cầu chỉnh sửa;
  - `V5`: wallet transactions/idempotency;
  - `V6`: conversations/messages/read state;
  - `V7`: reviews/constraints.
  - `V8`: phần trăm, ghi chú và thời gian cập nhật tiến độ dự án.
- Job transitions dùng pessimistic lock và ghi `job_status_history`.
- Message endpoint kiểm tra participant từ JWT và quan hệ job.
- Review kiểm tra participant, trạng thái completed và unique `(job, reviewer)`.
- Top-up dùng `BigDecimal`, wallet row lock, unique `(wallet, idempotency_key)` và ledger append-only.

## Frontend

- React 19, TypeScript, Vite.
- Route chính: `/jobs`, `/post-project`, `/proposals`, `/projects`, `/messages`, `/wallet`, `/profile`, `/profile/:userId`, `/rankings`.
- UI dùng dữ liệu API thật, có empty/error/loading state cơ bản và responsive styles hiện hữu.
- Message polling mỗi 2 giây ở màn chat; danh sách hội thoại và dự án mỗi 4 giây; badge unread toàn cục mỗi 5 giây khi tab hiển thị.

## Xác minh 2026-09-17

```text
Backend: .\mvnw.cmd test
PASS - 38 tests, 0 failures, 0 errors, 0 skipped

Frontend: npm run build
PASS - TypeScript và Vite production build

Flyway/PostgreSQL local
PASS - 7 migrations validated, schema version 7, Hibernate validate pass

API smoke hai tài khoản
PASS - register -> profile/skill -> top-up -> create job -> proposal -> accept
     -> conversation/messages -> submit -> accept submission -> two reviews -> rankings
PASS - retry cùng top-up idempotency key: balance 1.000.000, đúng 1 ledger entry
```

Smoke tạo dữ liệu local (job id 5 và hai tài khoản timestamped), không tác động production.

## Rủi ro và việc tiếp theo

Đánh giá chi tiết điều kiện triển khai nằm tại `docs/DEPLOYMENT_READINESS.md`. Hiện dự án phù hợp demo/staging, chưa đủ điều kiện xử lý tiền thật ở production.

- Chưa có escrow/funding/release/refund/withdraw; không dùng wallet demo như hệ thống tiền thật.
- Chưa có dispute/admin, attachment chat, WebSocket, notification push hệ điều hành hoặc upload file; thông báo trong ứng dụng hiện dùng polling nền.
- Ranking hiện aggregate trực tiếp trong service, phù hợp MVP; chuyển sang query/materialized aggregate khi dữ liệu thực tế lớn.
- Nên bổ sung browser E2E tự động và Testcontainers trong production-readiness phase.
- Port 8080 đã có một tiến trình khác khi verify; bản mới đã được smoke độc lập trên port 8081 và dừng sạch sau kiểm tra.
