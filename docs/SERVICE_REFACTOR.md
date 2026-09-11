# Tái cấu trúc tầng service

Ngày thực hiện: 2026-09-09

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
- Đã chạy `mvnw.cmd test`: build thành công, 3 test thành công, không có lỗi.

## Phạm vi chưa triển khai

Hiện dự án chưa có controller/API cho profile và skill, vì vậy hai service mới chưa được expose qua HTTP. Refactor này cũng chưa thay đổi mô hình lỗi cũ của login và các thao tác không tìm thấy user; chúng vẫn ném `RuntimeException` và có thể bị ánh xạ thành HTTP 500.

## Đề xuất tiếp theo

1. Tạo `UserProfileController` và `UserSkillController`; lấy email từ `Authentication`/`Principal`, không nhận email tùy ý từ request.
2. Thêm exception chuyên biệt cho sai thông tin đăng nhập và không tìm thấy tài nguyên; ánh xạ lần lượt về HTTP 401 và 404.
3. Thêm unit test cho ba service, ưu tiên rollback khi tạo user/ví lỗi, cập nhật profile từng phần, thêm skill trùng và xóa skill không tồn tại.
4. Bổ sung validation cho `UpdateProfileRequest` và chuẩn hóa tên skill (trim, quy tắc chữ hoa/thường).
5. Xử lý cảnh báo Lombok `@Builder` bằng `@Builder.Default` cho các collection/default value thực sự cần giữ khi dùng builder.
6. Tách mapper hoặc token abstraction chỉ khi xuất hiện use case thứ hai; hiện tại chưa cần thêm lớp trung gian.
