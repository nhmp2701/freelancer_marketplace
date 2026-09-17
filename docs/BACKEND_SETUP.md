# Hướng dẫn cài đặt và khởi động backend

Tài liệu này dành cho môi trường phát triển local trên Windows PowerShell. Cấu hình mặc định của dự án là:

| Thành phần | Giá trị local |
|---|---|
| PostgreSQL | Docker image `postgres:15-alpine` |
| Database | `freelance_db` |
| Username | `postgres` |
| Password | `change-me` |
| Cổng PostgreSQL trên máy | `5433` |
| Cổng backend | `8080` |

> `change-me` chỉ là giá trị mẫu cho môi trường local. Hãy thay bằng mật khẩu riêng và không dùng giá trị này ở production.

## 1. Chuẩn bị công cụ

Cài và khởi động:

1. Java JDK 21. Kiểm tra bằng `java -version`.
2. Docker Desktop. Chờ Docker Desktop báo engine đã chạy.
3. Git và PowerShell.

Maven không cần cài riêng vì repository đã có Maven Wrapper (`Backend/mvnw.cmd`).

Mở PowerShell tại thư mục gốc repository, là thư mục có `Backend`, `Frontend` và file `.env.example`.

## 2. Tạo file `.env` cho Docker Compose

Chạy tại thư mục gốc:

```powershell
Copy-Item .env.example .env
```

Mở `.env` và bảo đảm phần PostgreSQL có đúng các dòng sau:

```dotenv
POSTGRES_DB=freelance_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=change-me
POSTGRES_PORT=5433
```

Không commit file `.env`. File này đã được `.gitignore` loại trừ vì có thể chứa secret local.

## 3. Khởi động PostgreSQL

Vẫn tại thư mục gốc, chạy:

```powershell
docker compose --env-file .env -f Backend/docker-compose.yml up -d
docker compose --env-file .env -f Backend/docker-compose.yml ps
```

Kết quả mong đợi là container `freelance_postgres` có trạng thái `Up ... (healthy)` và mapping cổng `5433->5432`.

Có thể kiểm tra chính xác cấu hình Compose đã nhận:

```powershell
docker compose --env-file .env -f Backend/docker-compose.yml config
```

Trong kết quả, `POSTGRES_PASSWORD` phải khớp giá trị bạn đã cấu hình, database là `freelance_db`, user là `postgres`, và published port là `5433`.

## 4. Cấp biến môi trường cho Spring Boot

Đây là bước dễ bị bỏ sót: `--env-file .env` chỉ cấp biến cho Docker Compose. Spring Boot chạy bằng Maven trong terminal khác **không tự đọc** file `.env` ở root.

Trong đúng cửa sổ PowerShell sẽ dùng để chạy backend, nhập:

```powershell
$env:POSTGRES_HOST = 'localhost'
$env:POSTGRES_PORT = '5433'
$env:POSTGRES_DB = 'freelance_db'
$env:POSTGRES_USER = 'postgres'
$env:POSTGRES_PASSWORD = 'change-me'
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:CORS_ALLOWED_ORIGINS = 'http://localhost:5173'
```

Các biến `$env:...` chỉ tồn tại trong cửa sổ PowerShell hiện tại. Nếu đóng terminal, hãy khai báo lại trước khi chạy backend.

`JWT_SECRET` có thể bỏ trống ở local; backend sẽ sinh khóa tạm thời và token đăng nhập cũ sẽ mất hiệu lực sau khi restart. Nếu muốn token ổn định giữa các lần restart, đặt secret local dài ít nhất 32 byte:

```powershell
$env:JWT_SECRET = 'local-only-secret-must-be-at-least-32-bytes'
```

## 5. Chạy backend

```powershell
Set-Location Backend
.\mvnw.cmd spring-boot:run
```

Backend khởi động thành công khi log cho biết ứng dụng đã `Started` và không còn lỗi kết nối PostgreSQL/Flyway. API chạy tại `http://localhost:8080/api/v1`.

Flyway tự tạo hoặc nâng cấp schema khi ứng dụng khởi động. Không cần tự chạy file SQL.

## 6. Kiểm tra nhanh

Mở một cửa sổ PowerShell khác tại thư mục gốc:

```powershell
docker exec -e PGPASSWORD=change-me freelance_postgres `
  psql -h 127.0.0.1 -U postgres -d freelance_db `
  -c "select current_database(), current_user;"
```

Kết quả đúng phải chứa `freelance_db` và `postgres`.

Kiểm tra backend bằng endpoint public:

```powershell
Invoke-RestMethod http://localhost:8080/api/v1/jobs
```

## 7. Sửa lỗi mật khẩu không khớp

### Trường hợp A: backend dùng sai hoặc thiếu biến môi trường

Thông báo thường gặp:

```text
password authentication failed for user "postgres"
```

Kiểm tra giá trị trong terminal chạy backend:

```powershell
$env:POSTGRES_PASSWORD
```

Nếu không trả về giá trị bạn đã cấu hình, đặt lại rồi dừng và chạy lại backend:

```powershell
$env:POSTGRES_PASSWORD = 'change-me'
Set-Location Backend
.\mvnw.cmd spring-boot:run
```

Không sửa mật khẩu trực tiếp trong `Backend/src/main/resources/application.yml`. File đó cố ý lấy mật khẩu từ `${POSTGRES_PASSWORD}` để tránh commit secret vào source.

### Trường hợp B: PostgreSQL volume được tạo bằng mật khẩu cũ

`POSTGRES_PASSWORD` chỉ được Docker image dùng khi khởi tạo database lần đầu. Sửa `.env` sau đó không tự đổi mật khẩu bên trong volume đã tồn tại.

Để giữ nguyên dữ liệu, đổi password của user trong PostgreSQL:

```powershell
docker exec -it freelance_postgres psql -U postgres -d postgres
```

Tại dấu nhắc `postgres=#`, chạy:

```sql
ALTER USER postgres WITH PASSWORD 'change-me';
\q
```

Sau đó restart backend và chạy lại lệnh kiểm tra ở mục 6.

### Trường hợp C: có nhiều volume hoặc từng chạy Compose bằng tên project khác

Xem container và volume hiện có:

```powershell
docker ps -a --filter name=freelance_postgres
docker volume ls --filter name=postgres_data
```

Luôn dùng cùng một câu lệnh từ thư mục gốc để tránh tạo nhầm Compose project:

```powershell
docker compose --env-file .env -f Backend/docker-compose.yml up -d
```

Nếu dữ liệu local không cần giữ, có thể tạo lại database sạch. **Lệnh sau xóa toàn bộ dữ liệu PostgreSQL của Compose project hiện tại:**

```powershell
docker compose --env-file .env -f Backend/docker-compose.yml down -v
docker compose --env-file .env -f Backend/docker-compose.yml up -d
```

Chỉ dùng cách này sau khi chắc chắn không cần dữ liệu trong database local.

### Trường hợp D: cổng `5433` đang bị ứng dụng khác chiếm

Kiểm tra:

```powershell
Get-NetTCPConnection -LocalPort 5433 -ErrorAction SilentlyContinue
```

Có thể đổi `POSTGRES_PORT` trong `.env`, ví dụ thành `5434`. Khi đó phải đặt cùng giá trị cho backend:

```powershell
$env:POSTGRES_PORT = '5434'
```

Sau khi đổi cổng, tạo lại container bằng `docker compose ... up -d` và restart backend.

## 8. Các lệnh thường dùng

```powershell
# Xem log PostgreSQL
docker compose --env-file .env -f Backend/docker-compose.yml logs postgres

# Dừng container nhưng giữ dữ liệu
docker compose --env-file .env -f Backend/docker-compose.yml down

# Khởi động lại PostgreSQL
docker compose --env-file .env -f Backend/docker-compose.yml up -d

# Chạy test backend (không cần PostgreSQL local)
Set-Location Backend
.\mvnw.cmd test
```

## 9. Cấu hình nào điều khiển phần nào?

| File/biến | Vai trò | Có nên sửa? |
|---|---|---|
| `.env` | Giá trị local mà lệnh Compose đọc | Có, nhưng không commit |
| `.env.example` | Mẫu cấu hình cho thành viên mới | Có khi chuẩn local thay đổi |
| `Backend/docker-compose.yml` | Tạo PostgreSQL, port và volume | Thường không cần sửa |
| `Backend/src/main/resources/application.yml` | Ánh xạ biến môi trường vào datasource Spring | Không đặt password trực tiếp |
| `$env:POSTGRES_PASSWORD` | Password của tiến trình backend hiện tại | Phải khớp password trong PostgreSQL |

Luồng giá trị đúng là:

```text
.env (mật khẩu riêng) -> Docker Compose -> PostgreSQL user postgres
PowerShell $env:POSTGRES_PASSWORD (cùng mật khẩu) -> Spring Boot -> kết nối PostgreSQL
```

Hai nhánh trên phải cùng database, username, password và port.
