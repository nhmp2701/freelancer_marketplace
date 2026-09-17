# Đánh giá hiện trạng và điều kiện triển khai

> Ngày đánh giá: 2026-09-17
> Phạm vi: Spring Boot backend, React/Vite frontend, PostgreSQL, luồng dự án và ví nội bộ.

## 1. Kết luận ngắn

Dự án **đủ để demo hoặc triển khai vào môi trường thử nghiệm nội bộ**, nhưng **chưa đủ điều kiện vận hành production có tiền thật**.

Luồng marketplace chính đã hoạt động: đăng nhập, hồ sơ, đăng dự án, gửi/chọn đề xuất, nhắn tin, cập nhật tiến độ, bàn giao, nghiệm thu, giải ngân ví nội bộ và đánh giá. Backend có authorization theo người tham gia, Flyway, transaction, khóa dữ liệu ví và sổ giao dịch.

Các khoảng trống lớn trước production là CI/CD, kiểm thử trình duyệt, giám sát, backup/restore, rate limit, cơ chế thu hồi token, dispute/refund/withdraw hoàn chỉnh và tích hợp nhà cung cấp thanh toán thật.

## 2. Hiện trạng theo thành phần

| Thành phần | Hiện trạng | Đánh giá |
|---|---|---|
| Backend | Spring Boot, Java 21, JWT, JPA, PostgreSQL, Flyway | Tốt cho MVP |
| Frontend | React, TypeScript, Vite, responsive cơ bản | Tốt cho MVP |
| Cơ sở dữ liệu | Migration có version, Hibernate `validate` | Tốt |
| Authentication | JWT và route protection | Cần refresh/revoke strategy |
| Authorization | Kiểm tra owner/participant ở nghiệp vụ chính | Cần audit bảo mật toàn API |
| Tin nhắn/thông báo | Polling nền, unread state | Dùng được; chưa phải push/WebSocket |
| Ký quỹ/giải ngân | Row lock, idempotency, ledger hai phía | Chỉ là ví nội bộ/demo |
| Sao kê | Xuất CSV từ dữ liệu ví đã xác thực | Đủ cho MVP |
| Test | Unit/security tests backend và frontend build | Thiếu E2E/Testcontainers |
| Vận hành | Chưa có health, metrics, alert, CI/CD | Chưa đạt production |

## 3. Điều kiện bắt buộc trước khi deploy production

### Bảo mật

- [ ] Dùng secret manager; tuyệt đối không để JWT secret hoặc mật khẩu DB trong repository/image.
- [ ] Bật HTTPS bắt buộc, HSTS và cookie/token policy phù hợp mô hình deploy.
- [ ] Chốt refresh token, thu hồi token, thời hạn phiên và xử lý tài khoản bị khóa.
- [ ] Rate limit cho đăng nhập, đăng ký, gửi tin, tạo đề xuất và thao tác ví.
- [ ] Audit IDOR trên mọi API nhận `userId`, `jobId`, `proposalId`, `conversationId`.
- [ ] Thiết lập CSP, `X-Content-Type-Options`, frame policy và kiểm tra XSS từ nội dung người dùng.
- [ ] Quét dependency, secret và container image trong CI.

### Tiền và dữ liệu

- [ ] Xác định ví hiện tại là demo hay tích hợp nhà cung cấp thanh toán được cấp phép.
- [ ] Hoàn thiện hoàn tiền, rút tiền, hủy dự án và tranh chấp bằng state machine rõ ràng.
- [ ] Thêm khóa/idempotency và test cạnh tranh cho mọi nhánh hold/release/refund/withdraw.
- [ ] Có báo cáo đối soát: tổng phát sinh ledger phải khớp tổng biến động số dư.
- [ ] Không cho sửa/xóa ledger; phân quyền truy cập sao kê và log audit.
- [ ] Mã hóa backup, kiểm thử restore định kỳ và quy định retention.

### Kiểm thử và chất lượng

- [ ] CI bắt buộc chạy backend test, frontend typecheck/build, format check và dependency scan.
- [ ] Testcontainers PostgreSQL cho migration và transaction trên DB thật.
- [ ] E2E tối thiểu cho đăng nhập → đăng dự án → đề xuất → ký quỹ → bàn giao → giải ngân.
- [ ] Test concurrency cho chọn đề xuất và nghiệm thu lặp/đồng thời.
- [ ] Test responsive, accessibility, trình duyệt hỗ trợ và dữ liệu lớn.
- [ ] Có môi trường staging dùng cấu hình gần production và dữ liệu giả lập.

### Vận hành

- [ ] Dockerfile/image bất biến cho backend và frontend; không chạy dev server ở production.
- [ ] Health/readiness endpoint kiểm tra ứng dụng và kết nối DB.
- [ ] Structured logging, correlation ID, metrics, dashboard và cảnh báo lỗi/tỷ lệ 5xx.
- [ ] Migration chạy theo quy trình deploy một lần, có backup và kế hoạch rollback tương thích.
- [ ] Cấu hình CORS theo đúng domain production, không dùng wildcard.
- [ ] Định nghĩa SLO, quy trình incident, người chịu trách nhiệm và kênh cảnh báo.

## 4. Tiêu chí cho phép phát hành

Chỉ phát hành production khi tất cả điều kiện sau cùng đạt:

1. Pipeline CI/CD xanh và không thể bỏ qua các bước test/security bắt buộc.
2. Luồng E2E quan trọng chạy thành công trên staging từ build sẽ đưa lên production.
3. Không còn lỗ hổng mức Critical/High chưa có phương án chấp nhận rủi ro bằng văn bản.
4. Backup và restore đã được diễn tập thành công.
5. Monitoring phát hiện được lỗi API, DB, độ trễ và sai lệch giao dịch.
6. Luồng tiền có đối soát, idempotency và quy trình xử lý tranh chấp/hoàn tiền.
7. Secret, domain, CORS, HTTPS và cấu hình profile production đã được kiểm tra độc lập.
8. Có kế hoạch rollback và người trực xử lý trong thời gian phát hành.

## 5. Thứ tự khuyến nghị

1. Thêm CI với test/build/format check.
2. Thêm Testcontainers và E2E cho luồng marketplace hoàn chỉnh.
3. Hoàn thiện refund/withdraw/dispute và test cạnh tranh tiền.
4. Thêm health, metrics, structured logging và backup/restore.
5. Hardening bảo mật, staging rehearsal, sau đó mới cân nhắc payment provider thật.

## 6. Lệnh kiểm tra hiện tại

```powershell
cd Backend
.\mvnw.cmd test

cd ..\Frontend
npm run build
```

Trước mỗi lần bàn giao cần chạy thêm `git diff --check` và kiểm tra không có secret, file build hoặc dữ liệu thật trong diff.
