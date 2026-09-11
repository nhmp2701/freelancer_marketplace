# Hướng phát triển tiếp theo

> Cập nhật: 2026-09-11  
> Nguyên tắc: ưu tiên một luồng end-to-end chạy được; chưa xây abstraction hoặc module chưa có use case thực tế.

## 1. Đích đến

### Milestone gần nhất — Foundation MVP

Một người dùng có thể:

1. đăng ký/đăng nhập;
2. cập nhật hồ sơ và kỹ năng;
3. xem/tìm danh sách công việc công khai;
4. xem chi tiết công việc;
5. đăng, sửa, hủy và xem job của mình;
6. sử dụng các luồng trên bằng frontend thật, dữ liệu thật từ API.

### MVP marketplace hoàn chỉnh

Hai người dùng có thể đi hết luồng:

```text
Client đăng job -> Freelancer gửi proposal -> Client chọn freelancer
-> Thực hiện/bàn giao -> Nghiệm thu -> Thanh toán -> Đánh giá
```

Wallet, escrow và dispute chỉ được coi là hoàn thành khi có ledger, audit và kiểm thử cạnh tranh/idempotency; không nên triển khai bằng cách chỉ cộng/trừ trường `balance`.

## 2. Thứ tự triển khai

### Giai đoạn 0 — Ổn định baseline (P0)

Mục tiêu: repository sạch, cấu hình an toàn và API hiện tại có hành vi đáng tin trước khi frontend phụ thuộc vào nó.

- [x] Hoàn tất layout làm việc với backend trong `Backend/`; phục hồi/cập nhật `.gitignore`; xác nhận `.env` và `target/` bị bỏ qua. Việc ghi nhận rename sạch trong lịch sử sẽ hoàn tất khi chủ dự án stage/commit worktree hiện tại.
- [x] Chuẩn hóa lệnh chạy từ root và cập nhật `README.md` với prerequisites, Docker, env và test.
- [x] Đưa JWT secret ra biến môi trường bắt buộc; tách logging SQL sang development profile.
- [x] Chuẩn hóa lỗi 400/401/403/404/409; không trả message exception nội bộ ở lỗi 500.
- [x] Xử lý token thiếu/sai/hết hạn và token của user không còn tồn tại thành 401 ổn định.
- [x] Cho phép public `GET /api/v1/jobs` và `GET /api/v1/jobs/{id}`; giữ các route `/me/*` yêu cầu JWT.
- [x] Thêm CORS theo origin cấu hình được cho frontend local.
- [x] Sửa 6 cảnh báo builder default và giữ kiểu tiền/reputation bằng `BigDecimal` xuyên suốt.
- [x] Thêm test cho auth, job ownership/status, security routes và error mapping.
- [x] Đưa schema sang Flyway migration có version và bỏ cơ chế mount `init.sql`.

Kết quả xác minh milestone ngày 2026-09-11:

```text
Backend/.\mvnw.cmd test
Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

Flyway schema history: version 0 (baseline) và version 1 đều success
GET /api/v1/jobs              -> 200
GET /api/v1/jobs/me/posted    -> 401 khi không có JWT
```

Điều kiện hoàn thành:

- `git status` chỉ còn thay đổi dự kiến;
- backend chạy từ môi trường sạch bằng tài liệu trong README;
- test không cần thao tác DB thủ công ngoài một lệnh setup đã ghi rõ;
- không còn secret development cố định trong source;
- API trả status đúng cho các nhánh lỗi chính.

### Giai đoạn 1 — Frontend thật cho capability hiện có (P0)

Mục tiêu: thay prototype điều hướng giả bằng một ứng dụng gọi backend thật.

#### 1.1. Chốt nền frontend

- [ ] Chọn một SPA stack TypeScript phù hợp cách deploy; với phạm vi hiện tại, một React/Vite app là đủ, chưa cần SSR.
- [ ] Trích design token, layout và component dùng chung từ `ux-ui-demo`; giữ prototype làm reference, không tiếp tục sửa từng file HTML lặp.
- [ ] Thiết lập router, API client, cấu hình environment và xử lý loading/error/empty state.
- [ ] Thiết lập auth state và route guard; tránh lưu dữ liệu nhạy cảm ngoài nhu cầu tối thiểu.

#### 1.2. Vertical slice theo thứ tự

- [ ] Đăng ký → nhận token → vào ứng dụng.
- [ ] Đăng nhập/đăng xuất → khôi phục phiên hợp lệ.
- [ ] Xem/cập nhật profile và thêm/gỡ skill.
- [ ] Danh sách job có filter, pagination và URL query.
- [ ] Chi tiết job dùng dữ liệu API.
- [ ] Đăng/sửa/hủy job và danh sách “job đã đăng”.

Điều kiện hoàn thành:

- không còn `alert` giả lập trong các luồng trên;
- refresh trang không làm hỏng route;
- lỗi validation và authorization được hiển thị rõ;
- có ít nhất một smoke/E2E test cho đăng nhập → xem jobs → tạo job.

### Giai đoạn 2 — Proposal và vòng đời công việc (P0 của marketplace)

Mục tiêu: tạo giá trị marketplace thật trước khi đầu tư vào admin và thanh toán thật.

#### Domain tối thiểu

- [ ] `Proposal`: job, freelancer, cover letter, bid amount, delivery time, status, timestamps.
- [ ] Quy tắc: một freelancer không gửi trùng proposal active cho cùng job; chủ job không tự proposal; chỉ job `OPEN` nhận proposal.
- [ ] Client chấp nhận đúng một proposal theo transaction, gán freelancer và chuyển job sang `IN_PROGRESS`.
- [ ] Freelancer được gán mới có thể submit; client sở hữu mới có thể accept/reject submission.
- [ ] Ghi lại lịch sử transition thay vì cho client sửa status tùy ý.

#### API/UI tối thiểu

- [ ] Gửi/rút/sửa proposal khi hợp lệ.
- [ ] Xem proposal của freelancer và danh sách proposal trên job của client.
- [ ] Chọn freelancer.
- [ ] Bàn giao và nghiệm thu.
- [ ] Dashboard “việc đang làm/đã đăng” dựa trên trạng thái thật.

Điều kiện hoàn thành:

- luồng job → proposal → chọn → submit → complete chạy end-to-end;
- mọi transition có authorization và test transaction/concurrency thiết yếu;
- `getMyAcceptedJobs` có đường nghiệp vụ thật để sinh dữ liệu.

### Giai đoạn 3 — Tin nhắn và đánh giá (P1)

- [ ] Tạo conversation gắn với job/proposal và giới hạn participant.
- [ ] Bắt đầu bằng HTTP + polling nếu đáp ứng trải nghiệm; chỉ thêm WebSocket khi realtime thực sự cần.
- [ ] Lưu message, read timestamp và pagination; chưa cần attachment ở bản đầu.
- [ ] Cho phép review sau `COMPLETED`, mỗi phía một review/job.
- [ ] Tính reputation từ dữ liệu review có thể truy vết, không nhận điểm tổng trực tiếp từ client.

Điều kiện hoàn thành: hai bên trao đổi trong phạm vi job và đánh giá một lần sau khi hoàn thành.

### Giai đoạn 4 — Ledger, wallet và escrow (P0 về an toàn tiền)

Thực hiện sau khi lifecycle công việc ổn định.

- [ ] Thiết kế ledger append-only: transaction, entry, direction, amount, currency, reference, status, idempotency key.
- [ ] Định nghĩa invariant: tổng debit = tổng credit; không âm ngoài chính sách; một sự kiện nghiệp vụ không settle hai lần.
- [ ] Funding job/escrow: available → locked.
- [ ] Complete job: locked client → available freelancer.
- [ ] Cancel/refund/dispute theo transition rõ ràng.
- [ ] Deposit/withdraw trước tiên dùng adapter giả lập hoặc sandbox; provider thật là một integration riêng.
- [ ] Thêm optimistic/pessimistic locking phù hợp, audit log và test concurrent requests.
- [ ] Nối các trang wallet/deposit/withdraw/statement trong prototype.

Điều kiện hoàn thành: có thể đối soát mọi thay đổi số dư từ ledger và retry request không làm nhân đôi tiền.

### Giai đoạn 5 — Dispute và admin (P1)

- [ ] Admin authorization theo method/route, không chỉ dựa vào việc token có role claim.
- [ ] Danh sách/lọc/khóa-mở user kèm lý do và audit.
- [ ] Quản lý job vi phạm theo chính sách rõ ràng.
- [ ] Dispute có evidence, assignee, timeline, resolution và settlement action.
- [ ] Dashboard lấy số liệu tổng hợp thật; không ưu tiên chart trước nghiệp vụ xử lý.

### Giai đoạn 6 — Production readiness (P1)

- [ ] CI chạy backend test, frontend lint/typecheck/test/build.
- [ ] Testcontainers hoặc môi trường test database tự dựng và tự dọn.
- [ ] Health/readiness, structured logging, request correlation và metrics tối thiểu.
- [ ] Rate limit cho auth và endpoint nhạy cảm.
- [ ] Refresh/revocation strategy cho token nếu phiên dài hạn là yêu cầu sản phẩm.
- [ ] Backup/restore, migration rollback policy và secret management.
- [ ] Accessibility, responsive verification, performance budget và browser support.
- [ ] Threat model ngắn cho auth, IDOR, XSS, upload, payment và admin.

## 3. Backlog ưu tiên ngay

| Thứ tự | Task | Lý do |
|---:|---|---|
| 1 | Ổn định Git layout và `.gitignore` | Tránh mất/track nhầm file trước mọi thay đổi khác |
| 2 | Chuẩn hóa secret, profile, 401/403/404/500 | Frontend cần API contract an toàn và ổn định |
| 3 | Test auth/job/security hiện có | Khóa hành vi trước khi nối UI |
| 4 | Tạo frontend shell từ design system prototype | Nền để chuyển từng màn hình sang dữ liệu thật |
| 5 | Nối auth end-to-end | Vertical slice nhỏ nhất chứng minh tích hợp |
| 6 | Nối profile và jobs | Tận dụng toàn bộ backend đã có |
| 7 | Thiết kế/triển khai proposal | Capability marketplace quan trọng nhất còn thiếu |

## 4. Các quyết định cần chốt nhưng chưa nên chặn Foundation MVP

- Một tài khoản có thể vừa là client vừa là freelancer hay phải chọn vai trò? Khuyến nghị giữ một `USER` làm cả hai cho MVP vì domain hiện tại đã hỗ trợ cách này.
- Frontend và backend deploy cùng origin hay khác origin? Quyết định này ảnh hưởng CORS và cách giữ token.
- MVP dùng tiền giả lập, sandbox hay tiền thật? Khuyến nghị fake/sandbox cho tới khi ledger và lifecycle đã được kiểm chứng.
- Chat cần realtime ngay hay polling đủ dùng? Khuyến nghị polling ở phiên bản đầu.
- Portfolio file/image cần upload hay chỉ URL? Khuyến nghị URL trước, upload sau khi có yêu cầu thật.

## 5. Quy tắc cắt phạm vi

Chưa làm trong Foundation MVP:

- microservice, event bus hoặc CQRS;
- mapper/factory/interface mới chỉ có một consumer;
- recommendation AI;
- nhiều currency;
- attachment chat và video call;
- dashboard analytics phức tạp;
- payment provider production.

Chỉ thêm các hạng mục trên khi có requirement và tiêu chí chấp nhận cụ thể.
