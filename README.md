# 🎯 Caro Game API

## 📋 Project Overview

A modern, scalable **Caro Game** REST API built with **Spring Boot 3.5.3** following clean architecture principles and international best practices.

## 🚀 Features

### 🔐 Authentication & Security
- **JWT-based authentication** with access & refresh tokens
- **Username-based login** (modern approach)
- **Secure password management** with OTP verification
- **Account management** with role-based access control

### 🏗️ Architecture
- **Clean Architecture** with layered design
- **SOLID principles** implementation
- **Interface segregation** for better maintainability
- **Consistent API responses** with standardized error handling

### 📚 API Documentation
- **Swagger/OpenAPI 3.0** with comprehensive examples
- **International standards** compliant documentation
- **Interactive API testing** interface
- **Detailed request/response schemas**

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.5.3
- **Security**: JWT, BCrypt password encoding
- **Database**: PostgreSQL with JPA/Hibernate
- **Cache**: Redis for OTP and session management
- **Documentation**: SpringDoc OpenAPI 3.0
- **Email**: SMTP integration for notifications
- **Build Tool**: Maven

## 🏃‍♂️ Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 14+
- Redis 6+

### Installation
```bash
# Clone repository
git clone https://github.com/MinhAnh-IT/Caro-Game.git
cd Caro-Game

# Install dependencies
mvn clean install

# Run application
mvn spring-boot:run
```

### API Documentation
Access Swagger UI at: http://localhost:8080/swagger-ui/index.html

## 📡 API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User authentication (username-based)
- `POST /api/auth/forgot-password` - Request password reset
- `POST /api/auth/reset-password` - Reset password with OTP
- `POST /api/auth/change-password` - Change current password
- `POST /api/auth/refresh-token` - Refresh access token
- `POST /api/auth/logout` - User logout

## 🏗️ Project Structure

```
src/main/java/com/vn/caro_game/
├── controllers/
│   ├── base/           # Base controller with common methods
│   ├── interfaces/     # Controller interfaces
│   └── AuthController.java
├── services/
│   ├── interfaces/     # Service interfaces
│   └── AuthService.java
├── repositories/       # Data access layer
├── dtos/              # Data transfer objects
│   ├── request/       # Request DTOs
│   └── response/      # Response DTOs
├── entities/          # JPA entities
├── configs/           # Configuration classes
├── integrations/      # External service integrations
│   ├── jwt/          # JWT service
│   ├── email/        # Email service
│   └── redis/        # Redis/OTP service
└── exceptions/        # Custom exceptions
```

## 🔧 Configuration

### Environment Variables
```properties
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=caro_game
DB_USERNAME=your_username
DB_PASSWORD=your_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=your_jwt_secret
JWT_ACCESS_EXPIRATION=3600
JWT_REFRESH_EXPIRATION=86400

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email
MAIL_PASSWORD=your_password
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthControllerTest

# Skip tests during build
mvn clean install -DskipTests
```

## 📈 Performance & Security

- **Rate limiting** on sensitive endpoints
- **Input validation** with detailed error messages
- **Secure password hashing** with BCrypt
- **Token-based authentication** with JWT
- **CORS configuration** for cross-origin requests
- **Exception handling** with global error management

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Developer**: MinhAnh-IT
- **Email**: dev@carogame.com
- **GitHub**: https://github.com/MinhAnh-IT

---

⭐ **Star this repo if you find it helpful!**
