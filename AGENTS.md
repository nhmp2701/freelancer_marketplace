# Quy trình làm việc tự động cho Codex

## Mục tiêu vận hành

Sau khi hoàn thành một task, Codex phải tự kiểm tra kết quả, cập nhật tài liệu và tiếp tục task sẵn sàng kế tiếp trong cùng milestone. Không dừng chỉ vì một thay đổi đã compile.

`docs/PROJECT_STATUS.md` là ảnh chụp hiện trạng.  
`docs/DEVELOPMENT_ROADMAP.md` là thứ tự ưu tiên và checklist nguồn.

## Auto-loop

Lặp quy trình sau khi người dùng yêu cầu triển khai một milestone, yêu cầu “auto-loop”, “tiếp tục”, “làm đến khi xong” hoặc giao một mục tiêu có nhiều task:

```text
DISCOVER -> SELECT -> DEFINE -> IMPLEMENT -> VERIFY -> REVIEW -> RECORD
    ^                                                        |
    +--------------------------------------------------------+
```

### 1. DISCOVER — Hiểu trạng thái thật

1. Đọc instruction gần nhất và `git status --short`.
2. Nếu root có `.codegraph/`, dùng `codegraph explore` trước `rg`/đọc file để tìm symbol, call path và blast radius.
3. Đọc phần code trực tiếp liên quan, test hiện có và hai tài liệu trong `docs/`.
4. Không sửa/xóa thay đổi chưa commit của người dùng nếu không thuộc task.

### 2. SELECT — Chọn việc kế tiếp

1. Chọn checkbox chưa hoàn thành có độ ưu tiên cao nhất trong milestone đang làm.
2. Chỉ chọn task có dependency đã xong và có thể kiểm chứng trong repository.
3. Nếu roadmap không còn task phù hợp, kết thúc loop và báo milestone hoàn thành.

### 3. DEFINE — Đặt ranh giới task

Trước khi code, tự ghi nhận ngắn gọn:

- outcome quan sát được;
- file/luồng dự kiến bị ảnh hưởng;
- lệnh hoặc test chứng minh hoàn thành;
- điều gì cố ý không làm.

Giữ task là vertical slice nhỏ nhất có giá trị. Không tự thêm dependency, abstraction hoặc tính năng “để sau dùng”.

### 4. IMPLEMENT — Thay đổi tối thiểu đúng chỗ

1. Tái sử dụng pattern và dependency có sẵn.
2. Sửa nguyên nhân gốc ở điểm dùng chung thay vì vá từng caller.
3. Với nghiệp vụ tiền, authentication, authorization và dữ liệu người dùng: không được giản lược validation, transaction, audit hoặc error handling cần thiết.
4. Không deploy, gửi dữ liệu ra ngoài, dùng paid service, sửa production, rotate secret hoặc thực hiện thao tác phá hủy nếu người dùng chưa giao rõ phạm vi đó.

### 5. VERIFY — Chứng minh task hoàn thành

Chạy theo thứ tự, dừng sớm ở lỗi để sửa:

1. test nhỏ nhất liên quan trực tiếp;
2. test/lint/typecheck/build của module bị ảnh hưởng;
3. smoke test luồng end-to-end nếu task đi qua API/UI;
4. `git diff --check`.

Không đánh dấu hoàn thành nếu chỉ “có vẻ đúng”. Nếu môi trường không cho chạy test, ghi rõ test nào chưa chạy và lý do.

### 6. REVIEW — Tự review trước khi chuyển task

1. Dùng CodeGraph kiểm tra caller/blast radius của symbol đã đổi khi phù hợp.
2. Xem `git diff` để phát hiện file ngoài phạm vi, secret, debug log, generated artifact và thay đổi format hàng loạt.
3. Kiểm tra security/authorization, nhánh lỗi, transaction và backward compatibility.
4. Không tự commit trừ khi người dùng yêu cầu.

### 7. RECORD — Ghi nhận và lặp

1. Đánh dấu checkbox roadmap chỉ khi Definition of Done đã đạt.
2. Cập nhật `PROJECT_STATUS.md` nếu capability hoặc rủi ro thực tế thay đổi.
3. Ghi kết quả verify ngắn gọn trong báo cáo cuối; không tạo nhật ký dài cho từng chỉnh sửa nhỏ.
4. Quay lại `DISCOVER` và chọn task kế tiếp mà không cần hỏi lại.

## Điều kiện dừng

Dừng auto-loop và báo rõ lý do khi xảy ra một trong các điều kiện:

- milestone/mục tiêu đã hoàn thành;
- cần quyết định sản phẩm làm thay đổi đáng kể data model, bảo mật, tiền hoặc UX;
- cần credential, quyền truy cập hoặc dịch vụ ngoài chưa có;
- bước kế tiếp là destructive, deploy/production hoặc vượt phạm vi người dùng đã giao;
- phát hiện thay đổi của người dùng xung đột trực tiếp và không thể bảo toàn;
- cùng một blocker/lỗi lặp lại sau 3 hướng xử lý hợp lý;
- không còn task “ready” trong roadmap.

Khi dừng vì blocker, nêu: bằng chứng, những gì đã thử, trạng thái repository và đúng một câu hỏi/đầu vào cần từ người dùng.

## Definition of Done chung

Một task chỉ `done` khi:

- tiêu chí hành vi đạt;
- test phù hợp pass;
- không làm hỏng build/lint/typecheck liên quan;
- lỗi và authorization quan trọng đã được xét;
- tài liệu/roadmap được cập nhật nếu trạng thái dự án thay đổi;
- diff không chứa secret, generated artifact hoặc thay đổi ngoài phạm vi.

## Báo cáo cuối mỗi vòng làm việc

Báo cáo ngắn gọn theo thứ tự:

1. outcome đã hoàn thành;
2. kiểm tra đã chạy và kết quả;
3. task roadmap đã hoàn thành/đang còn;
4. blocker hoặc quyết định cần người dùng, nếu có.
