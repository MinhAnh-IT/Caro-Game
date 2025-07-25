# Caro Game Authentication System

## Tổng quan
Hệ thống authentication cho game Caro được xây dựng theo mô hình Layer Architecture và tuân thủ nguyên tắc SOLID.

## Cấu trúc dự án

### Layer Architecture
```
Controllers -> Services -> Repositories -> Database
     ↓           ↓            ↓
   DTOs    Integrations   Entities
```

### Nguyên tắc SOLID được áp dụng

1. **Single Responsibility Principle (SRP)**
   - Mỗi class chỉ có một trách nhiệm duy nhất
   - `AuthService`: Xử lý logic authentication
   - `JwtService`: Quản lý JWT tokens
   - `EmailService`: Gửi email
   - `OtpService`: Quản lý OTP

2. **Open/Closed Principle (OCP)**
   - Sử dụng interfaces cho các service
   - Có thể mở rộng mà không cần sửa đổi code hiện tại

3. **Liskov Substitution Principle (LSP)**
   - Các implementation có thể thay thế interface mà không ảnh hưởng functionality

4. **Interface Segregation Principle (ISP)**
   - Chia nhỏ interfaces theo chức năng cụ thể

5. **Dependency Inversion Principle (DIP)**
   - Phụ thuộc vào abstractions (interfaces) thay vì concrete classes

## Cấu hình

### 1. Database (PostgreSQL)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/CaroGame
spring.datasource.username=postgres
spring.datasource.password=Minhanh123
```

### 2. Redis
```properties
spring.redis.host=localhost
spring.redis.port=6379
```

### 3. JWT
```properties
jwt.secret=mySecretKeyForJWTTokenGenerationAndVerificationThatShouldBe256Bits
jwt.access-token.expiration=3600000  # 1 hour
jwt.refresh-token.expiration=604800000  # 7 days
```

### 4. Email (Gmail)
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### 5. OTP
```properties
otp.expiration-minutes=5
otp.max-attempts=3
```

## API Endpoints

### Authentication APIs

#### 1. Đăng ký
```http
POST /api/auth/register
Content-Type: multipart/form-data

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "avatarUrl": "file"
}
```

#### 2. Đăng nhập
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}
```

#### 3. Quên mật khẩu
```http
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "test@example.com"
}
```

#### 4. Đặt lại mật khẩu
```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "email": "test@example.com",
  "otp": "123456",
  "newPassword": "newpassword123"
}
```

#### 5. Yêu cầu OTP thay đổi mật khẩu
```http
POST /api/auth/request-change-password-otp
Authorization: Bearer <access_token>
```

#### 6. Thay đổi mật khẩu
```http
POST /api/auth/change-password
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "currentPassword": "oldpassword",
  "newPassword": "newpassword123",
  "otp": "123456"
}
```

#### 7. Làm mới token
```http
POST /api/auth/refresh-token
Content-Type: application/json

{
  "refreshToken": "your-refresh-token"
}
```

#### 8. Đăng xuất
```http
POST /api/auth/logout
Authorization: Bearer <access_token>
```

## Bảo mật

### 1. Password Encoding
- Sử dụng BCrypt để mã hóa mật khẩu
- Salt được tạo tự động

### 2. JWT Security
- Access Token: 1 giờ
- Refresh Token: 7 ngày
- Token blacklist trong Redis khi logout

### 3. Account Protection
- Khóa tài khoản sau 5 lần đăng nhập sai
- Khóa 24 giờ

### 4. OTP Security
- OTP 6 số, có hiệu lực 5 phút
- Tối đa 3 lần thử sai
- Lưu trữ trong Redis

## Xử lý lỗi

### Global Exception Handler
- CustomException: Xử lý lỗi business logic
- Validation: Xử lý lỗi validate input
- Generic Exception: Xử lý lỗi không mong muốn

### Response Format
```json
{
  "success": true/false,
  "message": "Thông báo",
  "data": {}, // Dữ liệu trả về
  "errorCode": "ERROR_CODE" // Mã lỗi (nếu có)
}
```

## Cách chạy ứng dụng

### 1. Cài đặt dependencies
```bash
mvn clean install
```

### 2. Cấu hình database
- Tạo database PostgreSQL: `CaroGame`
- Cập nhật thông tin kết nối trong `application.properties`

### 3. Cấu hình Redis
- Cài đặt và chạy Redis server
- Default: localhost:6379

### 4. Cấu hình Email
- Tạo App Password cho Gmail
- Cập nhật thông tin email trong `application.properties`

### 5. Chạy ứng dụng
```bash
mvn spring-boot:run
```

## Testing

### Test với Postman/curl
1. Đăng ký tài khoản mới
2. Đăng nhập để lấy tokens
3. Test các API bảo mật với Bearer token
4. Test flow quên mật khẩu với OTP

### Database
- Kiểm tra data trong table `users`
- Verify password đã được hash

### Redis
- Kiểm tra OTP trong Redis
- Verify token blacklist khi logout

## Monitoring & Logging
- Sử dụng SLF4J cho logging
- Log chi tiết các hoạt động authentication
- Monitor Redis cho OTP và token blacklist
