# Hiện trạng dự án Freelancer Marketplace

> Cập nhật: 2026-09-13
> Nguồn đánh giá: mã nguồn hiện tại, CodeGraph, build/test và smoke test trình duyệt với PostgreSQL local.

## Tóm tắt

Dự án đang ở trạng thái **backend marketplace nền tảng + frontend mới đã kết nối lại một phần**.

- Backend Spring Boot có auth JWT, hồ sơ/kỹ năng, job và proposal/chọn freelancer.
- Frontend mới trong `Frontend/` dùng React 19, TypeScript và Vite. Các contract auth, danh sách/chi tiết/tạo job, hồ sơ công khai và gửi proposal đã khớp backend.
- Frontend mới chưa khôi phục toàn bộ capability của frontend trước: cập nhật hồ sơ/kỹ năng, sửa/hủy/job của tôi, danh sách/quản lý proposal và chọn freelancer chưa có UI hoàn chỉnh.
- Trang ví hiện chỉ là giao diện: backend chưa có API `/wallet`, transaction, nạp hoặc rút tiền. Không được xem đây là capability đã kết nối.
- Tin nhắn, bàn giao/nghiệm thu, thanh toán/escrow, đánh giá, tranh chấp và admin vẫn chưa hoàn chỉnh end-to-end.

## Kết nối frontend/backend đã xác minh

| Luồng | Trạng thái |
|---|---|
| Đăng ký, đăng nhập, lưu phiên trong tab | Đã kết nối |
| Danh sách và chi tiết job | Đã kết nối, dùng dữ liệu API thật |
| Tạo job | Đã kết nối; skills và deadline đã đúng contract backend |
| Gửi proposal | Đã kết nối qua `POST /jobs/{id}/proposals`; có bid, cover letter và delivery days |
| Xem hồ sơ hiện tại | Đã kết nối qua public profile `GET /users/{id}` |
| Cập nhật hồ sơ/kỹ năng | Chưa có trong frontend mới |
| Sửa, hủy, xem job đã đăng/đã nhận | Chưa có trong frontend mới |
| Quản lý/chấp nhận proposal | Backend có, frontend mới chưa có |
| Ví/giao dịch/nạp/rút | Backend chưa có API; frontend chưa thể kết nối |

Frontend không còn dùng dữ liệu demo để che lỗi tải job. API client hiện xử lý cả response rỗng và lỗi validation dạng field-map của backend.

## Backend hiện có

- Java 21, Spring Boot 4.1.1, Spring Security, Spring Data JPA.
- PostgreSQL 15 qua Docker Compose; Flyway tới schema `V3`.
- API auth, user profile/skills, tìm/tạo/sửa/hủy job, job cá nhân, CRUD proposal và accept proposal.
- Job lifecycle đã tới `IN_PROGRESS`; chưa có use case bàn giao `SUBMITTED` và nghiệm thu `COMPLETED` hoàn chỉnh.
- Mật khẩu PostgreSQL trong `application.yml` đã chuyển sang biến môi trường bắt buộc, không còn giá trị cố định trong source.

## Xác minh gần nhất

Ngày 2026-09-13:

```text
Frontend: npm run build
PASS - TypeScript và Vite production build

Browser smoke với backend/PostgreSQL local
PASS - register -> jobs -> job detail -> create job -> public profile -> submit proposal

Backend: .\mvnw.cmd test
PASS - 30 tests, 0 failures, 0 errors, 0 skipped
```

Smoke test tạo dữ liệu local phục vụ kiểm chứng (user, job và proposal); không tác động môi trường production.

## Cấu hình repository

- Giữ `.gitignore`, `.gitattributes` và `.env.example` ở root làm cấu hình chuẩn của monorepo.
- Đã xóa `Backend/.gitignore` và `Backend/.gitattributes` vì cấu hình root đã bao phủ toàn repository.
- `.env` local đã được chuyển từ `Backend/` về root và vẫn bị Git bỏ qua. Docker Compose được chạy từ root với `--env-file .env` như hướng dẫn trong README.
- Giữ `Backend/docker-compose.yml`; đây là file compose duy nhất hiện có.

## Việc sẵn sàng kế tiếp

1. Khôi phục UI quản lý proposal và chọn freelancer dựa trên API backend đã có.
2. Hoàn thiện backend + UI cho bàn giao và nghiệm thu.
3. Chỉ triển khai wallet sau khi có ledger, transaction và quy tắc idempotency/audit.
