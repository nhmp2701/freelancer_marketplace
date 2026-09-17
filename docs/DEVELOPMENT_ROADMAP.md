# Hướng phát triển tiếp theo

> Điều chỉnh sau khi thay toàn bộ frontend (2026-09-13): các checkbox Phase 1 cũ bên dưới là kết quả của frontend trước và không còn đại diện đầy đủ cho UI hiện tại. Checklist nguồn cho frontend mới là:
>
> - [x] Auth: đăng ký, đăng nhập, đăng xuất và phiên trong tab.
> - [x] Job: danh sách, chi tiết và tạo job bằng API thật.
> - [x] Proposal: gửi proposal bằng API thật.
> - [x] Profile: xem hồ sơ hiện tại qua API thật.
> - [ ] Profile: cập nhật thông tin và thêm/gỡ kỹ năng.
> - [ ] Job: sửa, hủy, danh sách đã đăng và đã nhận.
> - [ ] Proposal: danh sách, sửa/rút và client chấp nhận proposal.
> - [ ] Wallet: chờ backend ledger/transaction API; UI hiện tại chưa được xem là đã kết nối.

> Cập nhật: 2026-09-13
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
- [x] Hoàn thiện JWT theo môi trường: local/dev tự sinh khóa tạm thời an toàn; profile `prod` fail-fast nếu thiếu secret hoặc secret dưới 32 byte.
- [x] Tách service job theo command/query, gom mapping DTO và phân giải skill dùng chung để tìm luồng code theo use case.
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

- [x] Chọn một SPA stack TypeScript phù hợp cách deploy; đã tạo React/Vite app, chưa thêm SSR.
- [x] Trích design token, layout và component dùng chung từ `ux-ui-demo`; giữ prototype làm reference, không tiếp tục sửa từng file HTML lặp.
- [x] Thiết lập router, API client, cấu hình environment và xử lý loading/error/empty state.
- [x] Thiết lập auth state và route guard; phiên chỉ lưu trong `sessionStorage`, không lưu mật khẩu hoặc dữ liệu nhạy cảm khác.

#### 1.2. Vertical slice theo thứ tự

- [x] Đăng ký → nhận token → vào ứng dụng.
- [x] Đăng nhập/đăng xuất → khôi phục phiên hợp lệ trong tab hiện tại.
- [x] Xem/cập nhật profile và thêm/gỡ skill.
- [x] Danh sách job có filter, pagination và URL query.
- [x] Chi tiết job dùng dữ liệu API.
- [x] Đăng/sửa/hủy job và danh sách “job đã đăng”.

Điều kiện hoàn thành:

- không còn `alert` giả lập trong các luồng trên;
- refresh trang không làm hỏng route;
- lỗi validation và authorization được hiển thị rõ;
- có ít nhất một smoke/E2E test cho đăng nhập → xem jobs → tạo job.

### Giai đoạn 2 — Proposal và vòng đời công việc (P0 của marketplace)

Mục tiêu: tạo giá trị marketplace thật trước khi đầu tư vào admin và thanh toán thật.

#### Domain tối thiểu

- [x] `Proposal`: job, freelancer, cover letter, bid amount, delivery time, status, timestamps.
- [x] Quy tắc: một freelancer không gửi trùng proposal active cho cùng job; chủ job không tự proposal; chỉ job `OPEN` nhận proposal.
- [x] Client chấp nhận đúng một proposal theo transaction, gán freelancer và chuyển job sang `IN_PROGRESS`.
- [ ] Freelancer được gán mới có thể submit; client sở hữu mới có thể accept/reject submission.
- [ ] Ghi lại lịch sử transition thay vì cho client sửa status tùy ý.

#### API/UI tối thiểu

- [x] Gửi/rút/sửa proposal khi hợp lệ.
- [x] Xem proposal của freelancer và danh sách proposal trên job của client.
- [x] Chọn freelancer.
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
| 7 | Hoàn thiện bàn giao/nghiệm thu | Proposal và chọn freelancer đã có; cần khép kín lifecycle job |

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

## 6. Cập nhật triển khai 2026-09-17

Các capability sau đã hoàn thành end-to-end và được kiểm chứng bằng backend tests, frontend production build, Flyway migrations và smoke API hai tài khoản:

- [x] Profile: cập nhật thông tin và thêm/gỡ kỹ năng trên frontend mới.
- [x] Job: phân biệt job tự đăng, job đang làm và job người khác.
- [x] Proposal: hộp đã gửi/đã nhận, rút và chấp nhận proposal.
- [x] Dashboard dự án: “Tôi thuê”/“Tôi làm”, bàn giao, yêu cầu sửa và nghiệm thu.
- [x] Tin nhắn theo job bằng HTTP polling, participant-only, pagination và read state.
- [x] Review sau `COMPLETED`, một review mỗi phía/job và reputation từ aggregate.
- [x] Top freelancer tháng và freelancer uy tín từ dữ liệu completed/review.
- [x] Wallet demo: balance, history, top-up với ledger append-only, row lock và idempotency.
- [x] Skill picker đăng job có danh mục mở rộng và thêm kỹ năng tùy ý.

Backlog tiếp theo chưa nằm trong yêu cầu hoàn thành lần này:

- [ ] Escrow funding/release/refund/withdraw và dispute policy.
- [ ] Chat attachment/WebSocket chỉ khi polling không đáp ứng trải nghiệm thực tế.
- [ ] Browser E2E tự động, Testcontainers và CI production-readiness.

## 7. Cập nhật trải nghiệm cộng tác 2026-09-17

- [x] Thiết kế lại màn tin nhắn: avatar, tin gần nhất, thời gian, unread badge, auto-scroll và responsive mobile.
- [x] Hiển thị tổng số tin chưa đọc trên điều hướng desktop/mobile (`5+` từ 5 tin), tự đồng bộ ở mọi trang và xóa badge khi đã đọc.
- [x] Thêm tiến độ dự án (phần trăm, ghi chú, thời gian cập nhật), chỉ freelancer được gán cập nhật khi job `IN_PROGRESS`.
- [x] Tự đồng bộ tin nhắn mỗi 2 giây, hội thoại/dự án mỗi 4 giây và badge toàn cục mỗi 5 giây khi tab hoạt động; không cần refresh.

Xác minh: 36 backend tests pass, frontend production build pass, Flyway bổ sung schema `V8`.

## 8. Hoàn thiện quản lý theo vai trò và thù lao 2026-09-17

- [x] Tách trang Project thành tab “Tôi thuê” và “Tôi làm”; mỗi lần chỉ quản lý một vai trò.
- [x] Tách trang Proposal thành tab “Tôi tuyển” và “Tôi ứng tuyển”, tự đồng bộ mỗi 4 giây.
- [x] Thêm badge đỏ cho proposal mới/cập nhật trên tab và điều hướng desktop/mobile; đồng bộ nền mỗi 5 giây.
- [x] Rút gọn tiến độ thành số phần trăm, làm nổi bật liên kết sản phẩm bàn giao cho client.
- [x] Ẩn form đánh giá sau khi người dùng đã đánh giá, dựa trên dữ liệu backend.
- [x] Khi chọn proposal, khóa giá bid từ ví client vào Escrow; khi nghiệm thu, giải ngân đúng giá bid vào ví freelancer bằng ledger idempotent và row lock.

## 9. Chuẩn hóa code và khả năng triển khai 2026-09-17

- [x] Format toàn bộ Java bằng Google Java Format và toàn bộ TypeScript/TSX/CSS bằng Prettier.
- [x] Thêm lệnh format/check có thể chạy lại trong `Backend/format.ps1` và `Frontend/package.json`.
- [x] Bổ sung comment giải thích luồng dữ liệu frontend và các nghiệp vụ authorization/transaction quan trọng.
- [x] Đổi danh sách hội thoại sang tên dự án; tên đối tác chỉ hiển thị trong cuộc trò chuyện.
- [x] Thêm xuất sao kê CSV và tự đồng bộ ví để thấy giao dịch giải ngân.
- [x] Việt hóa các thuật ngữ người dùng nhìn thấy: đề xuất hợp tác, ký quỹ, người thực hiện, sổ giao dịch.
- [x] Thêm `docs/DEPLOYMENT_READINESS.md` đánh giá hiện trạng và checklist production.
