# Đánh giá lỗ hổng SOLID

Ngày đánh giá: 2026-09-09

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
- Quy tắc chuẩn hóa tên skill chưa được mô hình hóa; thay đổi quy tắc sẽ sửa trực tiếp `UserSkillServiceImpl`.

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
- Exception đang dùng `RuntimeException` chung nên tầng HTTP không thể ánh xạ lỗi nghiệp vụ ổn định nếu dựa vào type.

Đề xuất: ưu tiên exception có type trước. Chỉ thêm `TokenService` khi cần thay JWT hoặc muốn cô lập contract token trong test; không tạo abstraction dự phòng khi chưa có nhu cầu.

## Rủi ro liên quan cần ưu tiên

Các mục dưới đây không hoàn toàn là vi phạm SOLID nhưng ảnh hưởng trực tiếp đến độ an toàn và khả năng bảo trì.

### P0 — Xử lý lỗi HTTP

- Login sai, tài khoản khóa và user không tồn tại đang dùng `RuntimeException`.
- `GlobalExceptionHandler` biến các lỗi này thành HTTP 500 và nối message nội bộ vào response.

Phương án: tạo các exception tối thiểu như `InvalidCredentialsException` và `ResourceNotFoundException`; ánh xạ thành 401/404 với response ổn định, không rò thông tin nội bộ.

### P0 — Cấu hình bí mật và production logging

- `application.yml` có JWT secret mặc định trong source.
- SQL logging và `show-sql` đang bật mặc định.

Phương án: bắt buộc `JWT_SECRET` từ environment/secret manager; chuyển SQL logging sang profile development và tắt ở production.

### P1 — Tính đúng đắn dữ liệu

- Maven cảnh báo các giá trị khởi tạo bị Lombok `@Builder` bỏ qua, gồm collection của `User`/`Skill` và một số default value.
- `reputationScore` là `BigDecimal` trong entity nhưng được đổi sang `Double` trong response, có nguy cơ mất độ chính xác.
- Tên skill mới chỉ được trim, chưa có quy tắc không phân biệt hoa/thường; thao tác find-then-create vẫn có thể gặp race và phải dựa vào unique constraint để chặn trùng.

Phương án: dùng `@Builder.Default` ở nơi cần thiết, giữ `BigDecimal` xuyên suốt DTO, chuẩn hóa tên skill và xử lý lỗi unique constraint rõ ràng.

### P1 — Test và API boundary

- Auth use case chưa có test chuyên biệt. Profile và skill mới có các kiểm tra cơ bản nhưng chưa bao phủ nhánh lỗi, tạo skill mới và hành vi transaction.
- Chưa có API cho profile/skill.

Phương án: thêm unit test theo use case trước khi expose endpoint; controller phải lấy danh tính từ security context để người dùng không sửa hồ sơ/kỹ năng của tài khoản khác.

## Thứ tự triển khai khuyến nghị

1. Chuẩn hóa exception và HTTP status.
2. Di chuyển JWT secret/logging sang cấu hình theo môi trường.
3. Viết unit test cho auth, profile và skill.
4. Tạo controller profile/skill dựa trên authenticated principal.
5. Sửa builder defaults và kiểu `reputationScore`.
6. Chỉ trích xuất mapper, token provider hoặc policy khi có nhu cầu tái sử dụng/thay thế thực tế.
