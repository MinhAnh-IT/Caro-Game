# 🎯 Caro Game - Complete Multiplayer Gaming Platform

<div align="center">
  <h3>🌟 Modern Real-Time Multiplayer Caro Game Platform 🌟</h3>
  <p><em>Built with Spring Boot 3.5.3, WebSocket, JWT Authentication & Enhanced Game Features</em></p>
  
  ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-green.svg)
  ![Java](https://img.shields.io/badge/Java-17-orange.svg)
  ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-blue.svg)
  ![Redis](https://img.shields.io/badge/Redis-7+-red.svg)
  ![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-purple.svg)
  ![Tests](https://img.shields.io/badge/Tests-410%2B-brightgreen.svg)
</div>

---

## 📋 **Mục Lục**
1. [🌟 Tổng Quan Dự Án](#-tổng-quan-dự-án)
2. [🚀 Tính Năng Chính](#-tính-năng-chính)
3. [🏗️ Kiến Trúc Hệ Thống](#️-kiến-trúc-hệ-thống)
4. [🛠️ Công Nghệ Sử Dụng](#️-công-nghệ-sử-dụng)
5. [📦 Cài Đặt & Chạy](#-cài-đặt--chạy)
6. [📡 API Documentation](#-api-documentation)
7. [🔌 WebSocket Endpoints](#-websocket-endpoints)
8. [🎮 Game Features](#-game-features)
9. [🗄️ Database Schema](#️-database-schema)
10. [🧪 Testing](#-testing)
11. [📈 Performance & Monitoring](#-performance--monitoring)
12. [🔧 Configuration](#-configuration)
13. [🚀 Deployment](#-deployment)
14. [📝 Development Guide](#-development-guide)
15. [🤝 Contributing](#-contributing)

---

## 🌟 **Tổng Quan Dự Án**

**Caro Game** là một nền tảng game đa người chơi hiện đại được xây dựng với **Spring Boot 3.5.3**. Hệ thống cung cấp trải nghiệm chơi Caro (Tic-Tac-Toe) real-time với quản lý người dùng toàn diện, hệ thống bạn bè và các tính năng giao tiếp trực tiếp.

### 🏆 **Điểm Nổi Bật**
- ✨ **Real-time multiplayer gaming** với công nghệ WebSocket
- 🔐 **Hệ thống xác thực toàn diện** với JWT & OTP
- 👥 **Quản lý bạn bè nâng cao** với theo dõi trạng thái online
- 🏗️ **Clean Architecture** tuân theo nguyên tắc SOLID
- 🚀 **Production-ready** với testing toàn diện (410+ tests)
- 📚 **Tài liệu đầy đủ** với Swagger/OpenAPI 3.0
- 🎯 **Enhanced Game Features** với 2-step ready system và rematch

---

## 🚀 **Tính Năng Chính**

### 🔐 **Xác Thực & Bảo Mật**
- **JWT-based authentication** với access & refresh tokens (3600s/86400s expiration)
- **Username-based login** system (phương pháp hiện đại)
- **OTP verification** cho quản lý mật khẩu bảo mật qua email
- **Role-based access control** với phân quyền người dùng
- **Session management** với Redis caching
- **Secure password hashing** với mã hóa BCrypt

### 👥 **Quản Lý Người Dùng & Tính Năng Xã Hội**
- **Đăng ký & quản lý hồ sơ người dùng** với upload avatar
- **Hệ thống bạn bè nâng cao**:
  - Gửi/chấp nhận/từ chối lời mời kết bạn
  - Chặn/bỏ chặn người dùng
  - Theo dõi trạng thái bạn bè (online/offline)
  - Tìm kiếm người dùng theo username/email
- **Theo dõi hiện diện online** với Redis TTL
- **Giám sát hoạt động người dùng** qua các phiên

### 🎮 **Quản Lý Phòng Game**
- **Tạo phòng** với cài đặt tùy chỉnh:
  - Phòng công khai (mở cho tất cả)
  - Phòng riêng tư với mã tham gia 4 ký tự (ví dụ: "A3X7")
  - Quản lý sức chứa phòng (tối đa 2 người chơi)
- **Smart matchmaking**:
  - Quick play tự động ghép đôi
  - Tham gia phòng bằng ID hoặc mã
  - Rời phòng với dọn dẹp tự động
- **Quản lý vòng đời game**:
  - Trạng thái phòng: WAITING → PLAYING → FINISHED
  - Timeout game 10 phút với Redis TTL
  - Dọn dẹp phòng tự động sau khi hoàn thành

### 🎯 **Enhanced Game Features (Mới)**
- **2-step Ready System**:
  - Player phải click "Mark Ready" trước khi game bắt đầu
  - Game tự động start khi cả 2 players đều ready
  - Tracking chi tiết ready state của từng player
- **2-step Rematch Process**:
  - Step 1: Request rematch từ một player
  - Step 2: Accept rematch từ player còn lại
  - Tạo room mới tự động khi cả 2 đều accept
- **Enhanced Game State Tracking**:
  - Chi tiết tracking: WAITING_FOR_PLAYERS → WAITING_FOR_READY → IN_PROGRESS → FINISHED
  - GameEndReason: WIN, SURRENDER, LEAVE, TIMEOUT
  - Player states: NOT_READY → READY → IN_GAME

### 💬 **Chat & Giao Tiếp Real-time**
- **In-room chat** với tin nhắn real-time
- **Message persistence** với lưu trữ lịch sử
- **WebSocket broadcasting** cho tất cả người chơi trong phòng
- **Anti-spam protection** với rate limiting

### 📊 **Game History & Statistics**
- **Comprehensive game history** tracking:
  - Lịch sử tất cả games đã chơi
  - Win/Loss statistics
  - Game duration và timing
  - End reason tracking (normal, surrender, leave)
- **Pagination support** cho hiệu suất tốt
- **Data analytics** cho player performance

---

## 🏗️ **Kiến Trúc Hệ Thống**

### 📐 **Clean Architecture Pattern**
```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                   │
│  Controllers, WebSocket Handlers, Security Config      │
├─────────────────────────────────────────────────────────┤
│                    Application Layer                    │
│     Services, DTOs, Mappers, Business Logic           │
├─────────────────────────────────────────────────────────┤
│                     Domain Layer                        │
│     Entities, Enums, Domain Rules, Constants          │
├─────────────────────────────────────────────────────────┤
│                  Infrastructure Layer                   │
│   Repositories, External APIs, Database, Redis        │
└─────────────────────────────────────────────────────────┘
```

### 🔄 **WebSocket Architecture**
```
Client ←→ STOMP ←→ Spring WebSocket ←→ SimpMessagingTemplate ←→ Redis Pub/Sub
```

### 🗄️ **Database Architecture**
- **PostgreSQL** cho persistent data
- **Redis** cho session management, caching, và real-time features
- **Connection pooling** với HikariCP
- **Database migration** với comprehensive schema updates

---

## 🛠️ **Công Nghệ Sử Dụng**

### 🔧 **Backend Technologies**
- **Spring Boot 3.5.3** - Main framework
- **Spring Security 6** - Authentication & authorization
- **Spring Data JPA** - Database abstraction
- **Spring WebSocket** - Real-time communication
- **Redis** - Caching & session management
- **PostgreSQL 14+** - Primary database
- **MapStruct** - Object mapping
- **Swagger/OpenAPI 3.0** - API documentation

### 🔨 **Development Tools**
- **Java 17** - Programming language
- **Maven** - Build tool
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **Docker** - Containerization
- **Git** - Version control

### 📦 **Key Dependencies**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>3.5.3</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
</dependencies>
```

---

## 📦 **Cài Đặt & Chạy**

### 📋 **Yêu Cầu Hệ Thống**
- **Java 17+**
- **PostgreSQL 14+**
- **Redis 7+**
- **Maven 3.8+**

### 🚀 **Quick Start**

1. **Clone repository:**
```bash
git clone https://github.com/MinhAnh-IT/Caro-Game.git
cd Caro-Game
```

2. **Cài đặt database:**
```bash
# PostgreSQL
createdb CaroGame

# Chạy migration
psql -U postgres -d CaroGame -f migration_enhanced_game_features.sql
```

3. **Cấu hình application.properties:**
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/CaroGame
spring.datasource.username=postgres
spring.datasource.password=your_password

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# JWT
jwt.secret=your_jwt_secret_key_here
jwt.access-token-expiration=3600
jwt.refresh-token-expiration=86400

# Email (for OTP)
spring.mail.host=smtp.gmail.com
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
```

4. **Chạy ứng dụng:**
```bash
# Development mode
./mvnw spring-boot:run

# Production mode
./mvnw clean package
java -jar target/caro-game-0.0.1-SNAPSHOT.jar
```

5. **Truy cập ứng dụng:**
- **API Documentation:** http://localhost:8080/swagger-ui.html
- **WebSocket Test:** http://localhost:8080/test-gameroom-websocket.html
- **Health Check:** http://localhost:8080/actuator/health

---

## 📡 **API Documentation**

### 🔐 **Authentication APIs**
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | User registration | ❌ |
| POST | `/api/auth/login` | User login | ❌ |
| POST | `/api/auth/refresh-token` | Refresh JWT token | ❌ |
| POST | `/api/auth/logout` | User logout | ✅ |
| POST | `/api/auth/forgot-password` | Request password reset OTP | ❌ |
| POST | `/api/auth/reset-password` | Reset password with OTP | ❌ |
| POST | `/api/auth/request-change-password-otp` | Request change password OTP | ✅ |
| POST | `/api/auth/change-password` | Change password with OTP | ✅ |

### 👥 **User Management APIs**
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/users/profile` | Get user profile | ✅ |
| PUT | `/api/users/profile` | Update user profile | ✅ |
| POST | `/api/users/upload-avatar` | Upload user avatar | ✅ |
| GET | `/api/users/search` | Search users | ✅ |

### 🤝 **Friend Management APIs**
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/friends` | Get friends list | ✅ |
| POST | `/api/friends/request` | Send friend request | ✅ |
| POST | `/api/friends/accept/{requestId}` | Accept friend request | ✅ |
| POST | `/api/friends/decline/{requestId}` | Decline friend request | ✅ |
| DELETE | `/api/friends/{friendId}` | Remove friend | ✅ |
| POST | `/api/friends/block/{userId}` | Block user | ✅ |
| POST | `/api/friends/unblock/{userId}` | Unblock user | ✅ |

### 🎮 **Game Room APIs**
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/rooms` | Get public rooms (paginated) | ✅ |
| POST | `/api/rooms` | Create new room | ✅ |
| GET | `/api/rooms/{roomId}` | Get room details | ✅ |
| POST | `/api/rooms/{roomId}/join` | Join room by ID | ✅ |
| POST | `/api/rooms/join-by-code` | Join room by code | ✅ |
| POST | `/api/rooms/{roomId}/leave` | Leave room | ✅ |
| POST | `/api/rooms/quick-play` | Quick play matchmaking | ✅ |
| GET | `/api/rooms/current` | Get current user room | ✅ |
| GET | `/api/rooms/history` | Get user game history | ✅ |

### 💬 **Chat APIs**
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/rooms/{roomId}/chat` | Send chat message | ✅ |

---

## 🔌 **WebSocket Endpoints**

### 🌐 **Connection & Authentication**
```javascript
// WebSocket connection với JWT authentication
const socket = new WebSocket('ws://localhost:8080/ws?token=Bearer ' + jwtToken);
const stompClient = Stomp.over(socket);
```

### 📡 **STOMP Destinations**

#### **📤 Client → Server (Send)**
| Destination | Description | Payload |
|-------------|-------------|---------|
| `/app/room/{roomId}/join` | Join room | `{}` |
| `/app/room/{roomId}/leave` | Leave room | `{}` |
| `/app/room/{roomId}/ready` | Mark player ready | `{}` |
| `/app/room/{roomId}/move` | Make game move | `{row: 0, col: 1}` |
| `/app/room/{roomId}/chat` | Send chat message | `{content: "Hello!"}` |
| `/app/room/{roomId}/rematch/request` | Request rematch (2-step) | `{}` |
| `/app/room/{roomId}/rematch/accept` | Accept rematch (2-step) | `{}` |
| `/app/room/{roomId}/rematch` | Create rematch (legacy) | `{}` |

#### **📥 Server → Client (Subscribe)**
| Topic | Description | Event Types |
|-------|-------------|-------------|
| `/topic/room/{roomId}` | Room events | `ROOM_UPDATE`, `PLAYER_JOINED`, `PLAYER_LEFT`, `PLAYER_READY`, `GAME_STARTED`, `GAME_ENDED`, `REMATCH_REQUESTED`, `REMATCH_ACCEPTED`, `REMATCH_CREATED` |
| `/topic/room/{roomId}/moves` | Game moves | `MOVE_MADE`, `GAME_WON`, `GAME_DRAW` |
| `/topic/room/{roomId}/chat` | Chat messages | `CHAT_MESSAGE` |
| `/user/queue/notifications` | Personal notifications | `FRIEND_REQUEST`, `GAME_INVITATION` |

### 🎯 **Enhanced WebSocket Events**

#### **🔄 2-Step Ready System Events:**
```javascript
// 1. Player clicks "Mark Ready"
stompClient.send('/app/room/' + roomId + '/ready', {}, '{}');

// 2. Server broadcasts to all players
// Topic: /topic/room/{roomId}
{
  "type": "PLAYER_READY",
  "data": {
    "playerId": 123,
    "playerName": "john_doe",
    "readyState": "READY"
  }
}

// 3. When both players ready → auto-start game
{
  "type": "GAME_STARTED",
  "data": {
    "gameState": "IN_PROGRESS",
    "gameStartedAt": "2025-08-01T10:30:00Z",
    "currentTurn": "X"
  }
}
```

#### **🔄 2-Step Rematch System Events:**
```javascript
// Step 1: Request rematch
stompClient.send('/app/room/' + roomId + '/rematch/request', {}, '{}');
// Broadcast: REMATCH_REQUESTED

// Step 2: Accept rematch  
stompClient.send('/app/room/' + roomId + '/rematch/accept', {}, '{}');
// Broadcast: REMATCH_ACCEPTED

// Step 3: Auto-create new room
{
  "type": "REMATCH_CREATED",
  "data": {
    "newRoomId": 456,
    "message": "New rematch room created! Redirecting..."
  }
}
```

---

## 🎮 **Game Features**

### 🎯 **Enhanced Game Flow**

#### **🔄 2-Step Ready System:**
1. **Room Creation/Join**: Player tham gia phòng với trạng thái `NOT_READY`
2. **Mark Ready**: Player click "Mark Ready" → trạng thái `READY`
3. **Auto Game Start**: Khi cả 2 players đều `READY` → game tự động bắt đầu
4. **Game Progress**: Players chơi game với trạng thái `IN_GAME`

#### **🔄 2-Step Rematch Process:**
1. **Game End**: Game kết thúc → hiển thị "Request Rematch" button
2. **Request Phase**: Player 1 click "Request Rematch" → trạng thái `REQUESTED`
3. **Accept Phase**: Player 2 thấy "Accept Rematch" button → click accept
4. **Room Creation**: Tự động tạo room mới → cả 2 players được redirect

### 🎲 **Game Rules & Logic**
- **15x15 board** Caro game (có thể cấu hình)
- **5 in a row** to win (horizontal, vertical, diagonal)
- **Turn-based** gameplay với time limit
- **Real-time synchronization** cho tất cả moves
- **Anti-cheat** validation ở server-side

### 📊 **Game States & Transitions**
```
WAITING_FOR_PLAYERS → WAITING_FOR_READY → IN_PROGRESS → FINISHED
                   ↓                    ↓              ↓
                PLAYER_JOIN        BOTH_READY      GAME_END
```

### 🏆 **Game End Conditions**
- **WIN**: 5 in a row achieved
- **DRAW**: Board full without winner
- **SURRENDER**: Player surrenders
- **LEAVE**: Player leaves during game
- **TIMEOUT**: Time limit exceeded

---

## 🗄️ **Database Schema**

### 📊 **Core Tables**

#### **👥 users**
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    display_name VARCHAR(100),
    password VARCHAR(255) NOT NULL,
    avatar_url TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    is_active BOOLEAN DEFAULT TRUE
);
```

#### **🎮 game_rooms**
```sql
CREATE TABLE game_rooms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    game_state VARCHAR(30) NOT NULL DEFAULT 'WAITING_FOR_PLAYERS',
    rematch_state VARCHAR(20) NOT NULL DEFAULT 'NONE',
    rematch_requester_id BIGINT REFERENCES users(id),
    new_room_id BIGINT REFERENCES game_rooms(id),
    game_started_at TIMESTAMP,
    game_ended_at TIMESTAMP,
    is_private BOOLEAN DEFAULT FALSE,
    join_code VARCHAR(20),
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW()
);
```

#### **👨‍👩‍👧‍👦 room_players**
```sql
CREATE TABLE room_players (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL REFERENCES game_rooms(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    is_host BOOLEAN DEFAULT FALSE,
    ready_state VARCHAR(20) NOT NULL DEFAULT 'NOT_READY',
    game_result VARCHAR(20) DEFAULT 'NONE',
    accepted_rematch BOOLEAN DEFAULT FALSE,
    has_left BOOLEAN DEFAULT FALSE,
    left_at TIMESTAMP,
    joined_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(room_id, user_id)
);
```

#### **📚 game_history**
```sql
CREATE TABLE game_history (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL REFERENCES game_rooms(id),
    winner_id BIGINT REFERENCES users(id),
    loser_id BIGINT REFERENCES users(id),
    end_reason VARCHAR(20) NOT NULL,
    game_started_at TIMESTAMP,
    game_ended_at TIMESTAMP,
    game_data TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);
```

### 🤝 **Social Features Tables**

#### **👫 friends**
```sql
CREATE TABLE friends (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    friend_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, friend_id)
);
```

#### **📬 friend_requests**
```sql
CREATE TABLE friend_requests (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL REFERENCES users(id),
    receiver_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

### 🔍 **Performance Indexes**
```sql
-- Game performance indexes
CREATE INDEX idx_game_rooms_status ON game_rooms(status);
CREATE INDEX idx_game_rooms_game_state ON game_rooms(game_state);
CREATE INDEX idx_game_rooms_rematch_state ON game_rooms(rematch_state);
CREATE INDEX idx_room_players_ready_state ON room_players(ready_state);
CREATE INDEX idx_game_history_user ON game_history(winner_id, loser_id);

-- Social features indexes
CREATE INDEX idx_friends_user_id ON friends(user_id);
CREATE INDEX idx_friend_requests_receiver ON friend_requests(receiver_id, status);
```

---

## 🧪 **Testing**

### 📊 **Test Coverage**
- **410+ Total Tests** across all layers
- **Unit Tests**: Service, Repository, Controller layers
- **Integration Tests**: WebSocket, Database, API endpoints
- **Security Tests**: Authentication, Authorization, JWT
- **Performance Tests**: Load testing, Concurrency

### 🔧 **Test Structure**
```
src/test/java/
├── controllers/        # Controller layer tests
├── services/          # Service layer tests  
├── repositories/      # Repository layer tests
├── configs/           # Configuration tests
├── entities/          # Entity tests
├── mappers/           # Mapper tests
├── utils/             # Utility tests
└── integration/       # Integration tests
```

### 🚀 **Running Tests**
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=GameRoomServiceTest

# Run tests with coverage
./mvnw test jacoco:report

# Run integration tests only
./mvnw test -Dtest=**/*IntegrationTest
```

### 📈 **Test Results Summary**
```
✅ AuthControllerTest: 4/4 PASSED
✅ GameRoomServiceTest: 27/27 PASSED
✅ UserMapperTest: 17/17 PASSED  
✅ GameMatchTest: 11/11 PASSED
✅ Entity Tests: 100% PASSED
✅ Security Tests: 100% PASSED
✅ WebSocket Tests: 100% PASSED
```

---

## 📈 **Performance & Monitoring**

### ⚡ **Performance Optimizations**
- **Database Connection Pooling** với HikariCP
- **Redis Caching** cho session và frequently accessed data
- **Lazy Loading** cho JPA relationships
- **Pagination** cho large datasets
- **Index Optimization** cho query performance

### 📊 **Monitoring & Metrics**
- **Spring Boot Actuator** cho health checks
- **Application metrics** với Micrometer
- **Database performance** monitoring
- **Redis performance** tracking
- **WebSocket connection** monitoring

### 🚀 **Scalability Features**
- **Stateless architecture** cho horizontal scaling
- **Redis pub/sub** cho multi-instance communication
- **Load balancer ready** configuration
- **Database optimization** với proper indexing
- **Connection pooling** cho database efficiency

---

## 🔧 **Configuration**

### 🔐 **Security Configuration**
```yaml
# JWT Configuration
jwt:
  secret: ${JWT_SECRET:your-secret-key-here}
  access-token-expiration: 3600  # 1 hour
  refresh-token-expiration: 86400 # 24 hours

# CORS Configuration  
cors:
  allowed-origins: "*"
  allowed-methods: "GET,POST,PUT,DELETE,OPTIONS"
  allowed-headers: "*"
  allow-credentials: true
```

### 🗄️ **Database Configuration**
```yaml
# PostgreSQL Configuration
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/CaroGame
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
```

### 🔴 **Redis Configuration**
```yaml
# Redis Configuration
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms
      jedis:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
```

### 📧 **Email Configuration**
```yaml
# Email Configuration (for OTP)
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME:your-email@gmail.com}
    password: ${EMAIL_PASSWORD:your-app-password}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

---

## 🚀 **Deployment**

### 🐳 **Docker Deployment**

#### **Dockerfile**
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app
COPY target/caro-game-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### **docker-compose.yml**
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_HOST=postgres
      - REDIS_HOST=redis
    depends_on:
      - postgres
      - redis
      
  postgres:
    image: postgres:14
    environment:
      POSTGRES_DB: CaroGame
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    volumes:
      - postgres_data:/var/lib/postgresql/data
      
  redis:
    image: redis:7-alpine
    volumes:
      - redis_data:/data

volumes:
  postgres_data:
  redis_data:
```

### ☁️ **Cloud Deployment**
- **AWS**: EC2, RDS, ElastiCache
- **Google Cloud**: Compute Engine, Cloud SQL, Memorystore
- **Azure**: App Service, Azure Database, Azure Cache
- **Heroku**: Với PostgreSQL và Redis add-ons

---

## 📝 **Development Guide**

### 🛠️ **Development Setup**
```bash
# Clone và setup
git clone https://github.com/MinhAnh-IT/Caro-Game.git
cd Caro-Game

# Install dependencies
./mvnw clean install

# Run in development mode
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 🔄 **Git Workflow**
```bash
# Feature development
git checkout -b feature/new-feature
git add .
git commit -m "feat: add new feature"
git push origin feature/new-feature

# Create Pull Request → Review → Merge
```

### 📚 **Code Standards**
- **Java Code Style**: Google Java Style Guide
- **Naming Convention**: camelCase for variables, PascalCase for classes
- **Documentation**: Javadoc for public methods
- **Testing**: Minimum 80% code coverage
- **Commit Messages**: Conventional Commits format

### 🔍 **Debugging**
```yaml
# Debug configuration
logging:
  level:
    com.vn.caro_game: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web.socket: DEBUG
```

---

## 🤝 **Contributing**

### 📋 **Contribution Guidelines**
1. **Fork** the repository
2. **Create feature branch** from `develop`
3. **Write tests** for new features
4. **Follow code standards** và conventions
5. **Submit pull request** với detailed description

### 🐛 **Bug Reports**
- Sử dụng GitHub Issues template
- Cung cấp steps to reproduce
- Bao gồm error logs và screenshots
- Specify environment details

### 💡 **Feature Requests**
- Describe use case và expected behavior
- Explain why feature is needed
- Consider implementation complexity
- Provide mockups nếu applicable

---

## 📞 **Support & Contact**

### 📧 **Contact Information**
- **Author**: MinhAnh-IT
- **Email**: hma2004.it@gmail.com
- **GitHub**: [MinhAnh-IT](https://github.com/MinhAnh-IT)

### 🔗 **Links**
- **Repository**: [Caro-Game](https://github.com/MinhAnh-IT/Caro-Game)
- **Issues**: [GitHub Issues](https://github.com/MinhAnh-IT/Caro-Game/issues)
- **API Docs**: http://localhost:8080/swagger-ui.html (when running)
- **WebSocket Test**: http://localhost:8080/test-gameroom-websocket.html

---

## 📄 **License**

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

<div align="center">
  <h3>🎉 Thank you for using Caro Game! 🎉</h3>
  <p><em>Happy Gaming! 🎮</em></p>
  
  ⭐ **Don't forget to give us a star if you like this project!** ⭐
</div>
