# Freelancer Marketplace — Liên Kết Việt

Monorepo cho nền tảng kết nối client và freelancer:

- `Backend/`: REST API Java 21 + Spring Boot + PostgreSQL.
- `Frontend/`: React/Vite application; `Frontend/ux-ui-demo/` được giữ làm UI reference.
- `docs/PROJECT_STATUS.md`: hiện trạng và mục tiêu.
- `docs/DEVELOPMENT_ROADMAP.md`: roadmap và checklist triển khai.

## Chạy backend local

Yêu cầu: Java 21 và Docker Desktop/Docker Compose.

```powershell
Copy-Item .env.example .env
docker compose --env-file .env -f Backend/docker-compose.yml up -d
$env:POSTGRES_PASSWORD = 'change-me'
# Tùy chọn ở local; bỏ qua để dùng khóa tạm thời được sinh an toàn.
$env:JWT_SECRET = 'replace-with-a-local-secret-of-at-least-32-bytes'
$env:CORS_ALLOWED_ORIGINS = 'http://localhost:5173'
Set-Location Backend
.\mvnw.cmd spring-boot:run
```

API mặc định kết nối PostgreSQL tại `localhost:5433`; `POSTGRES_PASSWORD` vẫn bắt buộc. Ở local/dev, nếu thiếu `JWT_SECRET`, backend tự sinh khóa HS256 an toàn trong bộ nhớ nên token cũ sẽ hết hiệu lực sau mỗi lần restart. Ở production, phải bật profile `prod` và cung cấp `JWT_SECRET` tối thiểu 32 byte; ứng dụng sẽ từ chối khởi động nếu secret thiếu hoặc yếu. Không dùng giá trị mẫu trong `.env.example` cho production.

## Chạy frontend local

Yêu cầu: Node.js 20.19+ hoặc 22.12+.

```powershell
Set-Location Frontend
Copy-Item .env.example .env.local
npm install
npm run dev
```

Frontend mặc định chạy tại `http://localhost:5173` và gọi API tại `http://localhost:8080/api/v1`. Thay `VITE_API_URL` trong `.env.local` nếu backend chạy ở địa chỉ khác.

## Chạy test

Các unit test và MVC slice test không cần PostgreSQL local:

```powershell
Set-Location Backend
.\mvnw.cmd test
```

Kiểm tra frontend:

```powershell
Set-Location Frontend
npm run lint
npm run build
```

## Tài liệu dự án

- [Hiện trạng và mục tiêu](docs/PROJECT_STATUS.md)
- [Hướng phát triển](docs/DEVELOPMENT_ROADMAP.md)
- [Quy trình auto-loop cho Codex](AGENTS.md)
