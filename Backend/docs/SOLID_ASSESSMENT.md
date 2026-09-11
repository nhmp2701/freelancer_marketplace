# Đánh giá lỗ hổng SOLID

Ngày đánh giá: 2026-09-09  
Cập nhật sau Foundation P0: 2026-09-11

Phạm vi đánh giá gồm controller, service, repository, entity, security và exception hiện có. Dependency/caller được đối chiếu bằng CodeGraph.

## Tóm tắt

| Nguyên tắc | Đánh giá | Nhận định chính |
|---|---|---|
| SRP | Đã cải thiện, còn điểm cần theo dõi | `UserService` đa trách nhiệm đã được tách; mapping DTO và orchestration auth vẫn nằm trong implementation tương ứng. |
| OCP | Trung bình | Role mặc định và cách phát token đang cố định trong `AuthServiceImpl`. |
| LSP | Chưa thấy vi phạm trực tiếp | Chưa có hệ phân cấp implementation đủ lớn để phát hiện thay thế sai hợp đồng. |
| ISP | Tốt hơn sau refactor | Ba interface nhỏ thay cho một interface chứa auth, profile và skill. |
| DIP | Khá | Controller phụ thuộc interface; service dùng repository abstraction và `PasswordEncoder`, nhưng phụ thuộc trực tiếp `JwtUtil`. |

## Phân tích theo nguyên tắc

### S — Single Responsibility Principle

Vấn đề ban đầu: `UserService` khai báo đăng ký, đăng nhập, hồ sơ và kỹ năng. Mỗi nhóm có lý do thay đổi khác nhau, khiến mọi consumer nhìn thấy một hợp đồng lớn hơn nhu cầu thực tế.

Đã xử lý: tách thành `AuthService`, `UserProfileService` và `UserSkillService`.

Điểm còn lại:

- `AuthServiceImpl.registerUser` vừa tạo user, tạo wallet, mã hóa mật khẩu và phát token. Đây vẫn có thể xem là một application use case hợp lệ; chỉ nên tách `WalletProvisioningService` hoặc token provider khi chúng được tái sử dụng hoặc có chính sách thay đổi độc lập.
- `UserProfileServiceImpl` tự mapping entity sang response. Chưa cần mapper riêng khi mapping chỉ có một nơi; nên tách khi mapping được lặp lại.
- `GlobalExceptionHandler` vừa định dạng validation error vừa ánh xạ exception nghiệp vụ. Quy mô hiện tại còn nhỏ, chưa cần chia handler.

### O — Open/Closed Principle

Các điểm khó mở rộng mà không sửa code hiện có:

- `AuthServiceImpl` gán cứng `Role.USER` khi đăng ký.
- `AuthServiceImpl` gọi trực tiếp `JwtUtil`; đổi sang session, opaque token hoặc nhiều token issuer sẽ buộc sửa service.
- Quy tắc chuẩn hóa tên skill hiện nằm trực tiếp trong service; thay đổi quy tắc vẫn sẽ sửa `UserSkillServiceImpl` và `JobServiceImpl`.

Đề xuất: chỉ tạo `TokenService` hoặc registration policy khi có ít nhất hai implementation/chính sách. Trước thời điểm đó, cấu trúc hiện tại ngắn và dễ đọc hơn.

### L — Liskov Substitution Principle

Chưa thấy vi phạm LSP trực tiếp. Mỗi interface hiện chỉ có một implementation và không có subtype thay đổi precondition, postcondition hoặc hành vi lỗi. Cần bảo vệ điều này bằng contract test nếu sau này thêm implementation khác.

### I — Interface Segregation Principle

Vấn đề ban đầu đã rõ: `AuthController` chỉ cần đăng ký/đăng nhập nhưng phải phụ thuộc `UserService` chứa cả profile và skill.

Đã xử lý: `AuthController` chỉ phụ thuộc `AuthService`; các contract profile và skill nằm trong package riêng. Không nên tạo thêm interface nhỏ hơn cho từng method vì chưa có consumer yêu cầu mức chia đó.

### D — Dependency Inversion Principle

Điểm tốt:

- Controller phụ thuộc `AuthService`, không phụ thuộc `AuthServiceImpl`.
- Service phụ thuộc các Spring Data repository interface.
- Auth phụ thuộc `PasswordEncoder` interface.

Khoảng trống:

- Auth phụ thuộc concrete class `JwtUtil`, làm unit test và thay đổi cơ chế token khó hơn.
- Một số quy tắc nghiệp vụ dùng exception chuẩn của Spring/Java thay vì taxonomy domain riêng; mức hiện tại đủ cho API nhỏ nhưng cần xem lại khi lifecycle job mở rộng.

Đề xuất: ưu tiên exception có type trước. Chỉ thêm `TokenService` khi cần thay JWT hoặc muốn cô lập contract token trong test; không tạo abstraction dự phòng khi chưa có nhu cầu.

## Rủi ro liên quan cần ưu tiên

Các mục dưới đây không hoàn toàn là vi phạm SOLID nhưng ảnh hưởng trực tiếp đến độ an toàn và khả năng bảo trì.

### P0 — Xử lý lỗi HTTP (đã xử lý 2026-09-11)

- Login sai/tài khoản khóa dùng `BadCredentialsException`; tài nguyên thiếu dùng `ResourceNotFoundException`.
- Handler ánh xạ 400/401/403/404/409; lỗi 500 dùng message chung và không lộ chi tiết nội bộ.

Kết quả: hành vi được khóa bằng unit test và MVC security test.

### P0 — Cấu hình bí mật và production logging (đã xử lý 2026-09-11)

- `application.yml` bắt buộc nhận `JWT_SECRET` từ môi trường.
- SQL logging và `show-sql` mặc định tắt, chỉ bật qua profile `dev`.

Kết quả: `.env.example` mô tả biến cần thiết mà không chứa secret sử dụng thực tế.

### P1 — Tính đúng đắn dữ liệu

- Builder defaults đã được khai báo rõ bằng `@Builder.Default` và Maven không còn cảnh báo tương ứng.
- `reputationScore` giữ `BigDecimal` xuyên suốt entity và response.
- Tên skill được trim và tìm không phân biệt hoa/thường; thao tác find-then-create vẫn có thể gặp race và phải dựa vào unique constraint để chặn trùng.

Phương án: dùng `@Builder.Default` ở nơi cần thiết, giữ `BigDecimal` xuyên suốt DTO, chuẩn hóa tên skill và xử lý lỗi unique constraint rõ ràng.

### P1 — Test và API boundary

- Auth, profile/skill cơ bản, job ownership/status, error mapping và security route đã có tổng cộng 16 test.
- Profile và skill đã có controller/API lấy danh tính từ principal; còn thiếu integration test database tự cô lập.

Phương án: thêm unit test theo use case trước khi expose endpoint; controller phải lấy danh tính từ security context để người dùng không sửa hồ sơ/kỹ năng của tài khoản khác.

## Thứ tự triển khai khuyến nghị sau Foundation P0

1. Thêm pagination/sort và chốt API contract phục vụ frontend.
2. Thêm integration test PostgreSQL tự cô lập khi bắt đầu thay đổi query/schema phức tạp.
3. Xử lý race khi tạo skill nếu tải đồng thời trở thành yêu cầu thực tế.
4. Chỉ trích xuất mapper, token provider hoặc policy khi có nhu cầu tái sử dụng/thay thế thực tế.
