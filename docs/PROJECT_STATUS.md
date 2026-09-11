# Hiện trạng và mục tiêu dự án Freelancer Marketplace

> Cập nhật: 2026-09-11  
> Nguồn đánh giá: mã nguồn hiện tại, lịch sử Git, CodeGraph và kết quả chạy test cục bộ.

## 1. Tóm tắt điều hành

Dự án đang ở giai đoạn **backend nền tảng + prototype UX/UI**, chưa phải một sản phẩm end-to-end có thể sử dụng thực tế.

- Backend Spring Boot đã có đăng ký, đăng nhập JWT, hồ sơ, kỹ năng và các thao tác cơ bản với công việc.
- Frontend hiện là **18 trang HTML prototype tĩnh** trong `Frontend/ux-ui-demo`; giao diện và luồng điều hướng đã khá đầy đủ nhưng chưa có ứng dụng frontend thực, chưa gọi API và chưa quản lý trạng thái đăng nhập.
- Các phần cốt lõi của một marketplace như proposal, chọn freelancer, hợp đồng, bàn giao, escrow, giao dịch, chat, đánh giá, tranh chấp và quản trị chưa có backend.
- Backend build thành công và 16/16 test pass; test unit/MVC hiện tại không phụ thuộc PostgreSQL local.
- Cấu trúc làm việc đã đặt backend trong `Backend/`, có cấu hình Git/env ở root; việc di chuyển chưa được stage/commit nên Git vẫn hiển thị backend cũ bị xóa và `Backend/` là thư mục mới.

## 2. Mục tiêu sản phẩm hiện tại

Tên và nội dung prototype cho thấy sản phẩm hướng tới một nền tảng freelance Việt Nam mang tên **Freelancer Marketplace**, kết nối khách hàng với freelancer, tập trung vào tính minh bạch và an toàn giao dịch.

### Mục tiêu MVP đề xuất

Cho phép hoàn thành một vòng đời công việc tối thiểu:

```text
Đăng ký/đăng nhập
  -> tạo hoặc tìm công việc
  -> gửi proposal
  -> khách hàng chọn freelancer
  -> trao đổi và bàn giao
  -> khách hàng nghiệm thu
  -> ghi nhận thanh toán
  -> hai bên đánh giá
```

Mục tiêu gần nhất nên là: **chuyển prototype thành một frontend thực và hoàn thiện vertical slice từ đăng nhập đến đăng/tìm/xem công việc**, sau đó mới bổ sung proposal và vòng đời giao dịch.

> Đây là mục tiêu được suy ra từ mã nguồn và prototype vì repository chưa có product specification chính thức.

## 3. Kiến trúc hiện tại

| Khu vực | Công nghệ/cấu trúc | Trạng thái |
|---|---|---|
| Backend | Java 21, Spring Boot 4.1.1, Spring MVC, Spring Data JPA, Spring Security | Có thể build/chạy |
| Xác thực | JWT, BCrypt, stateless security filter | Đã có luồng cơ bản |
| Database | PostgreSQL 15 qua Docker Compose, Flyway migration | Có schema users, wallets, skills, jobs; migration version `V1` |
| Frontend | HTML + Tailwind CDN + JavaScript nội tuyến | Chỉ là prototype tĩnh |
| Kiểm thử | JUnit/Spring Boot Test/Mockito/MockMvc | 5 file test, tổng 16 test |
| Migration | Flyway | Baseline version `0`, schema version `1` |
| CI/CD | Chưa thấy cấu hình | Chưa có |

Backend đang theo cấu trúc quen thuộc:

```text
Controller -> Service interface -> Service implementation -> Repository -> PostgreSQL
                  |
                  +-> DTO / Entity / Security / Exception
```

CodeGraph xác nhận các luồng chính đi từ `AuthController`, `UserProfileController`, `UserSkillController`, `JobController` tới các service và repository tương ứng.

## 4. Phần backend đã có

### Domain hiện hữu

- `User`: email, mật khẩu băm, tên, avatar, bio, reputation, role, status, skills và wallet.
- `Skill`: danh mục kỹ năng dùng chung cho user và job.
- `Wallet`: số dư khả dụng, số dư bị khóa và optimistic-lock version.
- `Job`: client, freelancer, tiêu đề, mô tả, ngân sách, deadline, kỹ năng và trạng thái.
- `JobStatus`: `OPEN`, `IN_PROGRESS`, `SUBMITTED`, `COMPLETED`, `CANCELLED`, `DISPUTED`.
- `Role`: mới có `USER` và `ADMIN`; chưa tách cứng client/freelancer. Cách này có thể phù hợp nếu một user được phép đóng cả hai vai trò.

### API hiện hữu

| API | Mức hoàn thiện |
|---|---|
| `POST /api/v1/auth/register` | Tạo user, tạo wallet, trả JWT |
| `POST /api/v1/auth/login` | Kiểm tra mật khẩu/trạng thái, trả JWT |
| `GET /api/v1/users/{userId}` | Xem profile công khai |
| `PUT /api/v1/users/me` | Cập nhật profile của chính user |
| `POST /api/v1/users/me/skills` | Thêm kỹ năng |
| `DELETE /api/v1/users/me/skills/{skillName}` | Gỡ kỹ năng |
| `GET /api/v1/jobs` | Tìm job theo status, budget, skill, keyword |
| `GET /api/v1/jobs/{id}` | Xem chi tiết job |
| `POST /api/v1/jobs` | Tạo job ở trạng thái `OPEN` |
| `PUT /api/v1/jobs/{id}` | Chủ job sửa khi còn `OPEN` |
| `DELETE /api/v1/jobs/{id}` | Chủ job chuyển trạng thái sang `CANCELLED` |
| `GET /api/v1/jobs/me/posted` | Danh sách job đã đăng |
| `GET /api/v1/jobs/me/accepted` | Danh sách job đã nhận |

### Giới hạn của lifecycle hiện tại

Mặc dù entity đã có `freelancer` và nhiều trạng thái, code chưa có use case để:

- freelancer gửi proposal;
- client chọn proposal/gán freelancer;
- chuyển job sang `IN_PROGRESS`;
- freelancer bàn giao và chuyển sang `SUBMITTED`;
- client nghiệm thu hoặc mở tranh chấp;
- chuyển tiền escrow;
- đánh giá sau hoàn thành.

Vì vậy `getMyAcceptedJobs` đã tồn tại nhưng hiện chưa có API nghiệp vụ nào tạo ra một job “đã nhận” theo đúng luồng.

## 5. Phần frontend prototype đã có

Prototype bao phủ hầu hết tầm nhìn sản phẩm:

| Nhóm | Trang |
|---|---|
| Public/Auth | `index.html`, `register.html`, `login.html` |
| Freelancer/Client | `jobs.html`, `job-detail.html`, `post-project.html`, `profile.html`, `invite.html`, `messages.html` |
| Ví | `wallet.html`, `deposit.html`, `withdraw.html`, `statement.html` |
| Admin | `admin.html`, `admin-users.html`, `admin-projects.html`, `admin-transactions.html`, `admin-disputes.html` |

Prototype có hệ màu, typography và mẫu màn hình tương đối nhất quán; thư mục Stitch còn lưu ảnh tham chiếu và `DESIGN.md`.

Tuy nhiên đây chưa phải frontend triển khai:

- không có `package.json`, framework, router hoặc build pipeline;
- không có `fetch`, Axios, WebSocket hay API client;
- dữ liệu người dùng, công việc, giao dịch và tin nhắn đều là dữ liệu mẫu viết trong HTML;
- form đăng nhập/đăng ký chưa submit tới backend;
- các nút chính chỉ đổi trang hoặc hiển thị `alert` giả lập;
- Tailwind và font/icon được tải từ CDN;
- nhiều đoạn JavaScript điều hướng được lặp lại ở từng file.

## 6. Ma trận khớp giữa prototype và backend

| Năng lực trong UI | Backend | Khoảng trống |
|---|---|---|
| Đăng ký/đăng nhập | Có | Cần nối frontend, xử lý token và lỗi |
| Hồ sơ/kỹ năng | Có một phần | Thiếu portfolio, lịch sử việc, review |
| Tìm/xem job | Có một phần | Đã public và có filter; còn thiếu pagination/sort |
| Đăng/sửa/hủy job | Có | Thiếu frontend thực và test authorization |
| Proposal/lời mời | Chưa có | Cần domain + API + state transition |
| Tin nhắn | Chưa có | Cần conversation/message + realtime hoặc polling |
| Ví/sao kê | Chỉ có entity wallet | Thiếu ledger, transaction, deposit/withdraw |
| Escrow/thanh toán | Chưa có | Cần thiết kế an toàn tiền và idempotency |
| Đánh giá | Chưa có | Cần review domain và cập nhật reputation |
| Tranh chấp | Chưa có | Cần dispute workflow và audit trail |
| Admin | Chưa có | Cần authorization `ADMIN`, API và audit |

## 7. Chất lượng và khả năng chạy

Kết quả kiểm tra ngày 2026-09-11:

```text
Backend/.\mvnw.cmd test
BUILD SUCCESS
Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
```

Điểm tích cực:

- application khởi động được với PostgreSQL 15.19 local;
- Flyway baseline/migration `V1` chạy thành công và Hibernate `validate` schema;
- service đã bắt đầu được tách theo trách nhiệm auth/profile/skill/job;
- thao tác sửa/hủy job có kiểm tra chủ sở hữu và trạng thái `OPEN`;
- auth, mapping lỗi, quyền truy cập job và JWT lỗi đã có test hồi quy;
- smoke test xác nhận job công khai trả `200`, route job cá nhân trả `401` khi không có JWT.

Hạn chế:

- chưa có integration test repository/database tự cô lập bằng Testcontainers;
- chưa test token JWT hết hạn bằng đồng hồ kiểm soát được và chưa có refresh/revocation;
- chưa có OpenAPI/API contract, pagination và chuẩn response chung;
- chưa có kiểm tra frontend vì chưa có frontend application.

## 8. Rủi ro cần xử lý sớm

### P0 — Cần hoàn tất ghi nhận Git layout

Các file cấu hình root, `.env.example`, README và `.gitignore` đã được chuẩn hóa; `.env` và build artifact không được track. Tuy nhiên worktree chưa được stage/commit nên Git vẫn ghi backend cũ ở root là deleted và `Backend/` là untracked. Cần review rồi stage toàn bộ thay đổi cùng lúc để Git nhận diện rename tốt nhất.

### Security/configuration đã ổn định cho Foundation MVP

- JWT secret là biến môi trường bắt buộc; SQL logging chỉ bật trong profile `dev`.
- Token sai/hết hạn/user đã bị xóa trả 401; lỗi 500 không lộ message nội bộ.
- Lỗi chính đã map sang 400/401/403/404/409 và có test.
- Danh sách/chi tiết job là public, route cá nhân vẫn cần JWT.
- CORS đọc danh sách origin từ biến môi trường.

### P0 — Tính đúng đắn nghiệp vụ

- trạng thái job tồn tại nhưng chưa có state machine/use case để chuyển trạng thái hợp lệ;
- wallet chỉ giữ balance, chưa có ledger bất biến để đối soát;
- chưa có idempotency/audit cho các thao tác tiền;
- tên skill đã được trim và tra cứu không phân biệt hoa/thường; vẫn có thể gặp race khi hai transaction đồng thời tạo cùng skill.

### P1 — Dữ liệu và bảo trì

- tìm job trả toàn bộ danh sách, chưa pagination và chưa quy định sort;
- validation response và error response chưa có một envelope thống nhất;
- chưa có CI và database test tự dựng/tự dọn.

## 9. Kết luận trạng thái

Dự án đã hoàn thành milestone ổn định backend Foundation P0: cấu hình an toàn hơn, lỗi HTTP nhất quán hơn, route công khai đúng chủ đích, schema có migration và 16 test hồi quy đều pass. Khoảng trống lớn nhất tiếp theo là **biến luồng mẫu thành một vertical slice có dữ liệu thật**: tạo frontend application rồi nối auth → profile → browse/detail/create job trước khi mở rộng sang proposal và thanh toán.
