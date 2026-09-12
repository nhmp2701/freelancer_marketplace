# Tái cấu trúc tầng service

Ngày thực hiện: 2026-09-09  
Cập nhật kiểm chứng: 2026-09-11

## Mục tiêu

Tách `UserService` theo từng trách nhiệm nghiệp vụ để phần xác thực, hồ sơ và kỹ năng có thể phát triển độc lập.

## Cấu trúc sau khi thay đổi

```text
service/
├── AuthService.java
├── impl/
│   └── AuthServiceImpl.java
├── profile/
│   ├── UserProfileService.java
│   └── impl/
│       └── UserProfileServiceImpl.java
└── skill/
    ├── UserSkillService.java
    └── impl/
        └── UserSkillServiceImpl.java
```

## Những thay đổi đã thực hiện

### Auth

- Đổi `UserService` thành `AuthService`.
- Đổi `UserServiceImpl` thành `AuthServiceImpl`.
- Chỉ giữ hai use case `registerUser` và `loginUser` trong service này.
- Cập nhật `AuthController` để phụ thuộc vào `AuthService`.
- Giữ nguyên hành vi đăng ký, tạo ví, mã hóa mật khẩu, kiểm tra trạng thái khóa và phát JWT.

### Profile

- Tạo `UserProfileService` với hai use case lấy và cập nhật hồ sơ.
- `getUserProfile` tải người dùng theo ID và trả về `UserProfileResponse`, bao gồm danh sách tên kỹ năng.
- `updateUserProfile` tải người dùng theo email và cập nhật từng trường không phải `null` từ `UpdateProfileRequest`.
- Các thao tác đọc/ghi được đặt trong transaction; thao tác đọc sử dụng `readOnly = true`.

### Skill

- Tạo `UserSkillService` với hai use case thêm và xóa kỹ năng của người dùng.
- Khi thêm, service tái sử dụng kỹ năng đã có hoặc tạo kỹ năng mới, sau đó liên kết với người dùng.
- Khi xóa, service chỉ gỡ liên kết khỏi người dùng, không xóa kỹ năng khỏi danh mục chung.
- `Set<Skill>` tiếp tục đảm nhiệm việc tránh liên kết trùng lặp trong cùng một user.

### Kiểm tra

- Dùng CodeGraph để xác định caller trước khi đổi tên và kiểm tra dependency graph sau refactor.
- Không còn import/caller chủ động nào dùng `UserService` hoặc `UserServiceImpl`.
- Thêm 2 unit test cho cập nhật profile từng phần và thêm/xóa skill.
- Đã chạy `mvnw.cmd test`: build thành công, tổng cộng 16 test thành công, không có lỗi.

## Phạm vi chưa triển khai

Profile và skill đã được expose qua controller/API, lấy danh tính người dùng từ principal. Mô hình lỗi cũng đã được chuẩn hóa: authentication trả 401, tài nguyên thiếu trả 404, authorization trả 403 và lỗi trạng thái trả 409.

## Đề xuất tiếp theo

1. Bổ sung integration test cho transaction rollback và repository query bằng PostgreSQL tự cô lập.
2. Thêm test nhánh tạo skill mới, skill trùng khác hoa/thường và xóa skill không tồn tại.
3. Tách mapper hoặc token abstraction chỉ khi xuất hiện use case thứ hai; hiện tại chưa cần thêm lớp trung gian.

## Bổ sung ngày 2026-09-12 — miền Job

- Tách `JobService` đa trách nhiệm thành `JobCommandService` và `JobQueryService` theo use case ghi/đọc.
- Đưa mapping `Job -> JobResponse` vào `JobMapper`, dùng chung cho cả hai service.
- Đưa quy tắc trim/find-or-create skill vào `SkillResolver`, dùng chung cho job và kỹ năng người dùng.
- Giữ transaction ghi ở command service và transaction `readOnly` ở query service; không thêm framework CQRS hay tầng kiến trúc mới.
