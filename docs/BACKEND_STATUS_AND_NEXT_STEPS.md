# Tình trạng backend và thứ tự triển khai tiếp theo

> Cập nhật: 2026-09-12
> Phạm vi: chỉ backend Spring Boot trong `Backend/`
> Mục đích: checklist để tự triển khai theo đúng thứ tự phụ thuộc.

## 1. Kết luận nhanh

Backend hiện đã đủ nền tảng cho một ứng dụng CRUD có đăng nhập, nhưng **chưa đủ nghiệp vụ để trở thành marketplace hoàn chỉnh**.

Đã dùng được:

- đăng ký, đăng nhập và xác thực bằng JWT;
- xem/cập nhật hồ sơ cá nhân;
- thêm/gỡ kỹ năng;
- đăng, sửa, hủy, tìm kiếm và xem công việc;
- kiểm tra chủ sở hữu khi sửa/hủy job;
- PostgreSQL + Flyway migration;
- xử lý lỗi HTTP cơ bản và CORS cấu hình được.

Khoảng trống quan trọng nhất là luồng:

```text
Freelancer gửi proposal
  -> Client chọn proposal
  -> Job bắt đầu
  -> Freelancer bàn giao
  -> Client nghiệm thu
  -> Thanh toán
  -> Hai bên đánh giá
```

Vì vậy, tính năng backend nên làm tiếp theo là **Proposal và vòng đời Job**, không phải chat, admin hay thanh toán thật.

## 2. Công nghệ và cấu trúc hiện tại

| Thành phần | Hiện trạng |
|---|---|
| Java | Java 21 |
| Framework | Spring Boot 4.1.1, Spring MVC |
| Database | PostgreSQL 15 |
| ORM/migration | Spring Data JPA, Flyway |
| Security | Spring Security, JWT HS256, BCrypt |
| Test | JUnit, Mockito, MockMvc |
| Kiến trúc | Controller → Service → Repository → PostgreSQL |

Các miền service đã được tách theo trách nhiệm:

```text
service/auth
service/profile
service/skill
service/job
```

Job đã tách command và query. Đây là mức tách hợp lý cho hiện tại; chưa cần microservice, event bus hay CQRS framework.

## 3. Những gì backend đã có

### 3.1. Authentication và security

- `POST /api/v1/auth/register`: tạo user, tạo wallet và trả access token.
- `POST /api/v1/auth/login`: xác thực email/mật khẩu/trạng thái tài khoản và trả token.
- Route auth và đọc job là public; các route còn lại yêu cầu JWT.
- Token sai, token hết hạn hoặc token của user đã bị xóa trả `401`.
- Secret JWT production bắt buộc có tối thiểu 32 byte; local/dev có thể dùng khóa tạm sinh trong bộ nhớ.
- CORS lấy origin từ cấu hình môi trường.

Chưa có:

- refresh token, logout/revocation phía server;
- quên/đổi mật khẩu và xác minh email;
- rate limit cho endpoint auth;
- audit đăng nhập hoặc quản lý phiên.

### 3.2. User, profile và skill

- `GET /api/v1/users/{userId}`: xem hồ sơ công khai.
- `PUT /api/v1/users/me`: cập nhật hồ sơ của chính user.
- `POST /api/v1/users/me/skills`: thêm skill.
- `DELETE /api/v1/users/me/skills/{skillName}`: gỡ skill.
- Skill được dùng chung giữa user và job, có chuẩn hóa tên ở service dùng chung.

Chưa có:

- portfolio, kinh nghiệm, lịch sử công việc;
- upload avatar/file;
- endpoint tìm kiếm freelancer;
- review thật để tính reputation.

### 3.3. Job

- `GET /api/v1/jobs`: lọc theo status, khoảng budget, skill và keyword.
- `GET /api/v1/jobs/{id}`: xem chi tiết.
- `POST /api/v1/jobs`: tạo job `OPEN`.
- `PUT /api/v1/jobs/{id}`: chủ job sửa khi job còn `OPEN`.
- `DELETE /api/v1/jobs/{id}`: chủ job chuyển job sang `CANCELLED`.
- `GET /api/v1/jobs/me/posted`: job do user đăng.
- `GET /api/v1/jobs/me/accepted`: job user được nhận.

Hạn chế hiện tại:

- tìm kiếm trả toàn bộ `List`, chưa pagination và sort;
- chưa kiểm tra rõ `minBudget <= maxBudget`;
- chưa có Proposal nên chưa có đường nghiệp vụ hợp lệ để gán `freelancer`;
- các trạng thái `IN_PROGRESS`, `SUBMITTED`, `COMPLETED`, `DISPUTED` mới chỉ tồn tại trong enum/schema;
- chưa có lịch sử chuyển trạng thái;
- query/specification chưa có test trực tiếp.

### 3.4. Wallet

Entity và bảng `wallets` đã có:

- `balance`;
- `lockedBalance`;
- optimistic-lock `version`.

Wallet hiện **chưa phải hệ thống thanh toán** vì chưa có ledger, transaction history, idempotency, deposit/withdraw hoặc escrow. Không nên cộng/trừ trực tiếp hai trường balance để triển khai thanh toán.

### 3.5. Database và kiểm thử

Migration `V1__initial_schema.sql` tạo:

- `users`, `wallets`, `skills`, `user_skills`;
- `jobs`, `job_skills`;
- index cơ bản cho email, status, client, freelancer và budget.

Kết quả kiểm tra ngày 2026-09-12:

```text
Backend/.\mvnw.cmd test
Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Test hiện chủ yếu là unit test và MVC slice test. Chưa có repository integration test chạy với PostgreSQL/Testcontainers và chưa có E2E API test đầy đủ.

## 4. Thứ tự nên code

Mỗi bước dưới đây là một vertical slice. Hoàn thành code, migration, authorization và test của bước hiện tại rồi mới sang bước kế tiếp.

### Bước 0 — Khóa baseline hiện tại

Mục tiêu: làm contract hiện có ổn định trước khi thêm domain mới.

- [ ] Hoàn tất và review việc di chuyển `AuthService` sang package `service/auth` đang có trong worktree.
- [ ] Chạy lại toàn bộ test sau khi stage/commit thay đổi hiện tại.
- [ ] Viết tài liệu API bằng OpenAPI hoặc ít nhất ghi rõ request/response/status code.
- [ ] Thêm pagination và sort cho `GET /api/v1/jobs`.
- [ ] Validate `minBudget`, `maxBudget` và quy tắc `minBudget <= maxBudget`.
- [ ] Thêm test cho `JobSpecification`, tạo job, hủy job và các nhánh authorization còn thiếu.
- [ ] Thêm repository integration test bằng PostgreSQL/Testcontainers khi bắt đầu viết query phức tạp.

Thứ tự file nên làm:

1. DTO/query contract;
2. repository/specification;
3. query service;
4. controller;
5. unit/MVC/repository test;
6. cập nhật OpenAPI.

Hoàn thành khi:

- danh sách job có `page`, `size`, sort mặc định ổn định;
- query sai trả `400`, không trả `500`;
- test module và build đều xanh.

### Bước 1 — Proposal

Đây là tính năng nên code đầu tiên sau baseline.

- [ ] Tạo enum `ProposalStatus`: `PENDING`, `ACCEPTED`, `REJECTED`, `WITHDRAWN`.
- [ ] Tạo entity `Proposal`: job, freelancer, cover letter, bid amount, delivery days, status, created/updated timestamps.
- [ ] Tạo Flyway migration `V2__create_proposals.sql`.
- [ ] Đặt unique constraint để một freelancer chỉ có một proposal active cho một job.
- [ ] Tạo repository và query có pagination.
- [ ] Tạo DTO request/response và mapper.
- [ ] Tạo service gửi, sửa và rút proposal.
- [ ] Tạo API để freelancer xem proposal của mình.
- [ ] Tạo API để chủ job xem proposal của job mình sở hữu.

Quy tắc bắt buộc:

- chỉ job `OPEN` nhận proposal;ư
- chủ job không được tự proposal vào job của mình;
- bid phải lớn hơn 0, delivery days phải hợp lệ;
- chỉ chủ proposal được sửa/rút;
- không trả cover letter hoặc dữ liệu riêng tư cho user không có quyền;
- kiểm tra unique constraint ở database, không chỉ kiểm tra bằng Java.

API gợi ý:

```text
POST   /api/v1/jobs/{jobId}/proposals
PUT    /api/v1/proposals/{proposalId}
DELETE /api/v1/proposals/{proposalId}
GET    /api/v1/proposals/me
GET    /api/v1/jobs/{jobId}/proposals
```

Nên code theo thứ tự:

1. migration + enum + entity;
2. repository;
3. DTO + mapper;
4. service command;
5. service query;
6. controller;
7. unit test + repository integration test + security test.

### Bước 2 — Chấp nhận proposal và khóa vòng đời Job

- [ ] Client chấp nhận đúng một proposal.
- [ ] Trong cùng transaction: proposal thành `ACCEPTED`, các proposal còn lại thành `REJECTED`, job được gán freelancer và chuyển `IN_PROGRESS`.
- [ ] Thêm bảng `job_status_history` để ghi from-status, to-status, actor, thời điểm và ghi chú.
- [ ] Không cung cấp API sửa status tùy ý; mỗi hành động nghiệp vụ có một command riêng.
- [ ] Dùng optimistic/pessimistic locking phù hợp để hai request đồng thời không thể nhận hai freelancer.

API gợi ý:

```text
POST /api/v1/proposals/{proposalId}/accept
```

Test quan trọng:

- user không phải chủ job nhận `403`;
- proposal của job khác không thể được chọn;
- job không còn `OPEN` thì không thể accept;
- gửi hai request accept cạnh tranh vẫn chỉ có một proposal thắng;
- `GET /api/v1/jobs/me/accepted` bắt đầu trả dữ liệu được sinh từ use case thật.

### Bước 3 — Bàn giao và nghiệm thu

- [ ] Tạo entity `Submission` hoặc `JobSubmission` gồm job, nội dung bàn giao, URL tài nguyên, version và timestamp.
- [ ] Freelancer được gán mới được submit job `IN_PROGRESS`.
- [ ] Submit chuyển job sang `SUBMITTED` và ghi status history.
- [ ] Client sở hữu có thể yêu cầu chỉnh sửa để quay lại `IN_PROGRESS`.
- [ ] Client nghiệm thu để chuyển `SUBMITTED` → `COMPLETED`.
- [ ] Định nghĩa rõ có cho submit nhiều version hay chỉ cập nhật bản hiện tại; khuyến nghị giữ lịch sử version.

API gợi ý:

```text
POST /api/v1/jobs/{jobId}/submissions
POST /api/v1/jobs/{jobId}/request-revision
POST /api/v1/jobs/{jobId}/complete
```

Chưa nối tiền thật ở bước này. Có thể hoàn thiện lifecycle bằng payment giả lập trước.

### Bước 4 — Review và reputation

- [ ] Tạo `Review` gắn với job, reviewer, reviewee, rating và comment.
- [ ] Chỉ cho review khi job `COMPLETED`.
- [ ] Mỗi phía chỉ được review phía còn lại một lần trên mỗi job.
- [ ] Đặt unique constraint tương ứng ở database.
- [ ] Tính reputation từ bảng review; không cho client gửi trực tiếp tổng điểm reputation.
- [ ] Trả summary review trong public profile và phân trang danh sách review.

Làm review trước chat vì review hoàn thiện giá trị cốt lõi của vòng đời job và ít phụ thuộc hạ tầng hơn realtime messaging.

### Bước 5 — Messaging tối thiểu

- [ ] Tạo `Conversation` gắn với job/proposal và danh sách participant được phép.
- [ ] Tạo `Message` với sender, content, created timestamp và read timestamp.
- [ ] Dùng HTTP + polling trước; chưa cần WebSocket.
- [ ] Phân trang theo cursor hoặc theo thời gian, không tải toàn bộ lịch sử.
- [ ] Kiểm tra participant ở mọi read/write để tránh IDOR.
- [ ] Chưa làm attachment ở phiên bản đầu.

### Bước 6 — Ledger, wallet và escrow

Chỉ bắt đầu khi các transition của job đã ổn định và có test.

- [ ] Thiết kế ledger append-only gồm transaction và entry.
- [ ] Mỗi nghiệp vụ có idempotency key.
- [ ] Bảo đảm tổng debit bằng tổng credit.
- [ ] Funding job: tiền khả dụng của client → tiền bị khóa.
- [ ] Complete job: tiền bị khóa → tiền khả dụng của freelancer.
- [ ] Cancel/refund không làm mất hoặc nhân đôi tiền.
- [ ] Mọi thay đổi số dư nằm trong transaction database và có audit/reference.
- [ ] Thêm test retry, rollback và concurrent request.
- [ ] Chỉ tích hợp cổng thanh toán sandbox sau khi ledger nội bộ đã đúng.

Không làm:

- sửa trực tiếp `wallet.balance` từ controller;
- dùng số thực `double/float` cho tiền;
- gọi payment provider thật trước khi có idempotency và reconciliation.

### Bước 7 — Dispute và admin

- [ ] Dispute gồm người mở, job, lý do, evidence, assignee, trạng thái, timeline và resolution.
- [ ] Settlement dispute phải đi qua ledger, không sửa balance trực tiếp.
- [ ] Bảo vệ admin API bằng authority lấy từ user hiện tại trong database.
- [ ] Khóa/mở user, xử lý job vi phạm và dispute đều phải có lý do + audit log.
- [ ] Viết test `USER` không thể gọi API admin dù tự sửa role claim trong token.

### Bước 8 — Production readiness

- [ ] CI chạy test/build backend trên mỗi pull request.
- [ ] Testcontainers cho migration và repository integration test.
- [ ] Actuator health/readiness và metrics tối thiểu.
- [ ] Structured logging + correlation ID; không log token/mật khẩu/nội dung nhạy cảm.
- [ ] Rate limit cho login/register/reset-password và thao tác tiền.
- [ ] Refresh/revocation token nếu sản phẩm cần phiên đăng nhập dài.
- [ ] Chính sách backup/restore và rollback migration.
- [ ] Threat model cho IDOR, JWT, XSS, upload, payment và admin.

## 5. Checklist áp dụng cho từng tính năng

Với mỗi tính năng, nên code và tự review theo vòng sau:

1. Viết use case và các actor được phép thực hiện.
2. Viết bảng chuyển trạng thái và nhánh lỗi trước khi viết controller.
3. Tạo migration mới; không sửa migration đã chạy ở môi trường dùng chung.
4. Tạo entity/repository tối thiểu.
5. Tạo request/response DTO; không trả thẳng entity JPA.
6. Viết service và đặt transaction boundary ở service.
7. Kiểm tra ownership/authorization bằng dữ liệu database.
8. Viết controller mỏng, chỉ parse input và trả HTTP response.
9. Viết test happy path, validation, unauthorized, forbidden, not found, conflict và concurrency nếu có.
10. Chạy test module, toàn bộ test, build và `git diff --check`.
11. Cập nhật OpenAPI và checklist tài liệu.

Một task chỉ được đánh dấu xong khi migration chạy được trên database sạch, hành vi lỗi có status code đúng và test chứng minh authorization quan trọng.

## 6. Danh sách ưu tiên rút gọn

Nếu chỉ cần nhìn một danh sách để bắt đầu code, hãy đi theo thứ tự này:

- [x] 1. Hoàn tất refactor AuthService đang dở và khóa baseline bằng test.
- [x] 2. Pagination/sort/validation cho job search.
- [ ] 3. OpenAPI cho API hiện có.
- [x] 4. Proposal: tạo/sửa/rút/xem.
- [x] 5. Accept proposal trong transaction và gán freelancer.
- [x] 6. Job status history cho transition `OPEN` → `IN_PROGRESS`; các transition bàn giao còn lại thực hiện ở bước kế tiếp.
- [ ] 7. Submission → revision → completion.
- [ ] 8. Review và tính reputation.
- [ ] 9. Messaging bằng HTTP polling.
- [ ] 10. Ledger + escrow + idempotency.
- [ ] 11. Dispute và admin audit.
- [ ] 12. CI, Testcontainers, observability và hardening production.

## 7. Các quyết định sản phẩm cần chốt trước khi đến bước liên quan

- Một user có thể vừa là client vừa là freelancer? Khuyến nghị MVP: có, tiếp tục dùng role `USER` cho cả hai.
- Proposal được sửa bao nhiêu lần và client có xem lịch sử giá cũ không?
- Bàn giao cho phép file upload hay chỉ URL? Khuyến nghị ban đầu: URL.
- Client được yêu cầu sửa tối đa bao nhiêu vòng?
- Khi nào tiền được giữ escrow: lúc đăng job hay lúc accept proposal?
- MVP dùng tiền giả lập, sandbox hay tiền thật? Khuyến nghị: giả lập/sandbox cho đến khi ledger được kiểm chứng.
- Dispute có làm dừng thời hạn nghiệm thu tự động không?

Các quyết định về proposal history và revision limit có thể chốt khi bắt đầu bước tương ứng. Quyết định về escrow phải chốt trước Bước 6 và không nên tự suy đoán trong code.

## 8. Phạm vi chưa nên làm

Để giữ backend dễ hoàn thành, chưa nên ưu tiên:

- microservice, Kafka/event bus hoặc CQRS framework;
- recommendation AI;
- nhiều loại tiền tệ;
- WebSocket khi polling vẫn đáp ứng được;
- file attachment/video call;
- dashboard analytics phức tạp;
- payment production;
- abstraction chỉ có một implementation và chưa giải quyết use case hiện tại.

Mốc backend MVP hợp lý là khi hai tài khoản có thể chạy trọn luồng:

```text
đăng job → gửi proposal → chọn freelancer → bàn giao
→ nghiệm thu → thanh toán giả lập qua ledger → review
```
