# Caro Game - Multiplayer Tic-Tac-Toe Platform

A comprehensive web-based multiplayer Caro (Tic-Tac-Toe) game platform built with Spring Boot and modern web technologies. The platform provides real-time gameplay, user management, friend systems, and comprehensive game statistics.

## Table of Contents

1. [Overview](#overview)
2. [Technology Stack](#technology-stack)
3. [System Architecture](#system-architecture)
4. [Features](#features)
5. [API Documentation](#api-documentation)
6. [Database Schema](#database-schema)
7. [Installation Guide](#installation-guide)
8. [Configuration](#configuration)
9. [Development Guide](#development-guide)
10. [Testing](#testing)
11. [Deployment](#deployment)
12. [Contributing](#contributing)

## Overview

The Caro Game platform is a full-featured multiplayer gaming system that allows users to play Caro (also known as Tic-Tac-Toe or Five-in-a-Row) in real-time with friends or other players. The system includes comprehensive user management, friend systems, game rooms, real-time chat, and detailed statistics tracking.

### Key Capabilities

- **Real-time Multiplayer Gaming**: Play Caro games with other players in real-time using WebSocket communication
- **User Management**: Complete user registration, authentication, and profile management system
- **Friend System**: Add friends, manage friend requests, and see online status
- **Game Rooms**: Create and join game rooms with customizable settings
- **Statistics Tracking**: Comprehensive game statistics including win/loss ratios, rankings, and game history
- **Game Replay**: View detailed replays of past games with move-by-move playback
- **Real-time Chat**: In-game chat system for communication during gameplay
- **Mobile Responsive**: Fully responsive design that works on desktop and mobile devices

## Technology Stack

### Backend
- **Java 17**: Modern Java with latest features and performance improvements
- **Spring Boot 3.5.3**: Enterprise-grade application framework
- **Spring Security**: JWT-based authentication and authorization
- **Spring WebSocket**: Real-time bidirectional communication with STOMP protocol
- **Spring Data JPA**: Object-relational mapping and database operations
- **Spring Validation**: Request validation and data integrity
- **Spring Mail**: Email notifications and user communications

### Database & Caching
- **MySQL 8.0**: Primary relational database for persistent storage
- **Redis**: In-memory caching for session management and online user tracking
- **Flyway**: Database migration and version control
- **Connection Pooling**: HikariCP for optimized database connections

### Documentation & Testing
- **Swagger/OpenAPI 3**: Comprehensive API documentation and testing interface
- **JUnit 5**: Unit and integration testing framework
- **Mockito**: Mocking framework for isolated testing
- **TestContainers**: Integration testing with real database containers

### Development Tools
- **Maven**: Dependency management and build automation
- **Lombok**: Reduced boilerplate code with annotations
- **MapStruct**: Type-safe bean mapping between DTOs and entities
- **SLF4J + Logback**: Structured logging and monitoring

## System Architecture

The application follows a layered architecture pattern with clear separation of concerns:

### Architecture Layers

```
┌─────────────────────────────────────┐
│         Presentation Layer          │
├─────────────────────────────────────┤
│  REST Controllers  │  WebSocket     │
│  - AuthController  │  Controllers   │
│  - GameController  │  - GameRoom    │
│  - UserController  │  - CaroGame    │
│  - FriendController│                │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│           Service Layer             │
├─────────────────────────────────────┤
│  Business Logic Services            │
│  - AuthService                      │
│  - GameRoomService                  │
│  - CaroGameService                  │
│  - UserProfileService              │
│  - FriendService                    │
│  - GameStatisticsService           │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│         Repository Layer            │
├─────────────────────────────────────┤
│  Data Access Objects               │
│  - UserRepository                  │
│  - GameRoomRepository              │
│  - GameMatchRepository             │
│  - GameHistoryRepository           │
│  - FriendRepository                │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│          Data Layer                 │
├─────────────────────────────────────┤
│    MySQL Database    │    Redis     │
│  - Persistent Data   │  - Sessions  │
│  - User Information  │  - Cache     │
│  - Game Records     │  - Online    │
│  - Statistics       │    Status    │
└─────────────────────────────────────┘
```

### Component Interactions

1. **Client Applications** communicate with the backend through:
   - REST APIs for standard CRUD operations
   - WebSocket connections for real-time game updates
   - HTTP/HTTPS for file uploads (avatars)

2. **Authentication Flow**:
   - JWT token-based stateless authentication
   - Redis session storage for online user tracking
   - Role-based access control for different user levels

3. **Real-time Communication**:
   - STOMP protocol over WebSocket for game moves
   - Topic-based message broadcasting for room updates
   - Point-to-point messaging for private communications

## Features

### User Management
- **Registration & Authentication**: Secure user registration with email verification
- **Profile Management**: Update profile information, change passwords, upload avatars
- **Password Recovery**: Email-based password reset with secure token verification
- **Online Status**: Real-time tracking of user online/offline status

### Friend System
- **Friend Requests**: Send and manage friend requests with accept/decline options
- **Friend List**: View all friends with their online status
- **User Search**: Search for users by username or display name
- **Privacy Controls**: Manage who can send friend requests

### Game System
- **Room Creation**: Create custom game rooms with specific settings
- **Room Discovery**: Browse and join available public game rooms
- **Private Rooms**: Create password-protected rooms for private games
- **Spectator Mode**: Watch ongoing games as a spectator
- **Game Settings**: Customize board size, win conditions, and time limits

### Gameplay Features
- **Real-time Moves**: Instant move synchronization across all clients
- **Turn Management**: Automatic turn switching with timeout handling
- **Win Detection**: Automatic detection of win conditions (5 in a row)
- **Draw Detection**: Automatic detection of draw/tie situations
- **Move Validation**: Server-side validation of all game moves
- **Game State Persistence**: All games are saved for future replay

### Statistics & Analytics
- **Personal Statistics**: Track wins, losses, draws, and win rates
- **Game History**: Complete history of all played games with replay capability
- **Rankings**: Global and friend-based player rankings
- **Performance Metrics**: Detailed performance analysis and trends
- **Achievement System**: Unlock achievements based on gameplay milestones

### Communication
- **In-game Chat**: Real-time chat during gameplay
- **Room Chat**: Communication in game rooms before and after games
- **Private Messaging**: Direct messages between friends
- **Emote System**: Quick reaction emotes during gameplay

## API Documentation

The application provides comprehensive REST API endpoints organized by functionality:

### Authentication APIs (`/api/auth`)
- `POST /register` - Register new user account
- `POST /login` - Authenticate user and receive JWT token
- `POST /refresh` - Refresh expired JWT tokens
- `POST /logout` - Logout and invalidate tokens
- `POST /forgot-password` - Initiate password reset process
- `POST /reset-password` - Complete password reset with token
- `POST /verify-email` - Verify email address with verification token
- `POST /resend-verification` - Resend email verification

### User Profile APIs (`/api/user-profile`)
- `GET /me` - Get current user profile information
- `PUT /update` - Update user profile details
- `POST /upload-avatar` - Upload user avatar image
- `PUT /change-password` - Change user password
- `DELETE /delete-account` - Delete user account

### Friend Management APIs (`/api/friends`)
- `GET /search` - Search users by username or display name
- `POST /request` - Send friend request to another user
- `GET /requests/received` - Get pending friend requests received
- `GET /requests/sent` - Get friend requests sent to others
- `POST /accept/{requestId}` - Accept received friend request
- `POST /decline/{requestId}` - Decline received friend request
- `DELETE /remove/{friendId}` - Remove friend from friends list
- `GET /list` - Get list of all friends

### Game Room APIs (`/api/rooms`)
- `GET /` - Get list of available game rooms with pagination
- `POST /create` - Create new game room
- `GET /{roomId}` - Get detailed information about specific room
- `POST /{roomId}/join` - Join existing game room
- `DELETE /{roomId}/leave` - Leave current game room
- `PUT /{roomId}/settings` - Update room settings (room creator only)
- `DELETE /{roomId}` - Delete game room (room creator only)
- `GET /{roomId}/players` - Get list of players in room

### Game APIs (`/api/v1/games`)
- `POST /{roomId}/move` - Make a move in the game
- `GET /{roomId}/state` - Get current game state
- `POST /{roomId}/surrender` - Surrender current game
- `POST /{roomId}/offer-draw` - Offer draw to opponent
- `POST /{roomId}/respond-draw` - Respond to draw offer

### Statistics APIs (`/api/statistics`)
- `GET /my-stats` - Get personal game statistics
- `GET /my-history` - Get paginated game history
- `GET /game-replay/{gameId}` - Get detailed game replay data
- `GET /top-players` - Get top players by win rate
- `GET /my-ranking` - Get current user's ranking

### Online Status APIs (`/api/online-status`)
- `GET /friends` - Get online status of all friends

### WebSocket Endpoints

The application uses STOMP protocol over WebSocket for real-time communication:

#### Connection Endpoint
- `ws://localhost:8080/game-websocket` - Main WebSocket connection endpoint

#### Game Room Topics
- `/topic/room/{roomId}` - General room updates and announcements
- `/topic/room/{roomId}/game` - Game-specific updates (moves, game state changes)
- `/topic/room/{roomId}/chat` - Room chat messages

#### User-specific Topics
- `/user/{userId}/queue/notifications` - Personal notifications
- `/user/{userId}/queue/friend-requests` - Friend request notifications

#### Message Destinations
- `/app/room/{roomId}/join` - Join a game room
- `/app/room/{roomId}/leave` - Leave a game room
- `/app/room/{roomId}/chat` - Send chat message to room
- `/app/game/{roomId}/move` - Make a game move
- `/app/game/{roomId}/surrender` - Surrender the game

## Database Schema

The application uses a well-normalized MySQL database schema:

### Core Entities

#### Users Table
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    avatar_url VARCHAR(500),
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE'
);
```

#### Game Rooms Table
```sql
CREATE TABLE game_rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_by BIGINT NOT NULL,
    is_private BOOLEAN DEFAULT FALSE,
    password_hash VARCHAR(255),
    max_players INT DEFAULT 2,
    room_status ENUM('WAITING', 'IN_GAME', 'FINISHED') DEFAULT 'WAITING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);
```

#### Game Matches Table
```sql
CREATE TABLE game_matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    player_x_id BIGINT NOT NULL,
    player_o_id BIGINT NOT NULL,
    current_player ENUM('X', 'O') DEFAULT 'X',
    game_status ENUM('WAITING', 'IN_PROGRESS', 'FINISHED') DEFAULT 'WAITING',
    result ENUM('X_WIN', 'O_WIN', 'DRAW', 'ONGOING') DEFAULT 'ONGOING',
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    board_state JSON,
    FOREIGN KEY (room_id) REFERENCES game_rooms(id),
    FOREIGN KEY (player_x_id) REFERENCES users(id),
    FOREIGN KEY (player_o_id) REFERENCES users(id)
);
```

#### Moves Table
```sql
CREATE TABLE moves (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    x_position INT NOT NULL,
    y_position INT NOT NULL,
    move_number INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (match_id) REFERENCES game_matches(id),
    FOREIGN KEY (player_id) REFERENCES users(id)
);
```

#### Friends Table
```sql
CREATE TABLE friends (
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'DECLINED', 'BLOCKED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (friend_id) REFERENCES users(id)
);
```

#### Game History Table
```sql
CREATE TABLE game_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    winner_id BIGINT,
    loser_id BIGINT,
    end_reason ENUM('WIN', 'DRAW', 'SURRENDER', 'TIMEOUT') NOT NULL,
    game_started_at TIMESTAMP NOT NULL,
    game_ended_at TIMESTAMP NOT NULL,
    total_moves INT DEFAULT 0,
    FOREIGN KEY (winner_id) REFERENCES users(id),
    FOREIGN KEY (loser_id) REFERENCES users(id)
);
```

### Relationships and Constraints

1. **User-Room Relationship**: Many-to-many through room_players table
2. **User-Friend Relationship**: Self-referencing many-to-many with status
3. **Room-Match Relationship**: One-to-many (room can have multiple matches)
4. **Match-Move Relationship**: One-to-many (match contains multiple moves)
5. **User-Game History**: Many-to-many tracking all games played

### Database Indexes

```sql
-- Performance indexes for common queries
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_game_rooms_created_by ON game_rooms(created_by);
CREATE INDEX idx_game_matches_room_id ON game_matches(room_id);
CREATE INDEX idx_moves_match_id ON moves(match_id);
CREATE INDEX idx_friends_user_id ON friends(user_id);
CREATE INDEX idx_friends_friend_id ON friends(friend_id);
CREATE INDEX idx_game_history_winner_id ON game_history(winner_id);
CREATE INDEX idx_game_history_loser_id ON game_history(loser_id);
```

## Installation Guide

### Prerequisites

Ensure you have the following installed on your development machine:

- **Java Development Kit (JDK) 17 or higher**
- **Maven 3.8 or higher**
- **MySQL 8.0 or higher**
- **Redis 6.0 or higher**
- **Git** for version control

### Step-by-Step Installation

1. **Clone the Repository**
```bash
git clone https://github.com/MinhAnh-IT/Caro-Game.git
cd Caro-Game
```

2. **Database Setup**

Create MySQL database and user:
```sql
CREATE DATABASE caro_game_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'caro_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON caro_game_db.* TO 'caro_user'@'localhost';
FLUSH PRIVILEGES;
```

3. **Redis Setup**

Start Redis server:
```bash
# On macOS with Homebrew
brew services start redis

# On Ubuntu/Debian
sudo systemctl start redis-server

# On Windows
redis-server
```

4. **Environment Configuration**

Create `src/main/resources/application-local.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/caro_game_db
spring.datasource.username=caro_user
spring.datasource.password=secure_password

# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=

# JWT Configuration
jwt.secret=your-256-bit-secret-key-here
jwt.access-token-expiration=86400000
jwt.refresh-token-expiration=604800000

# Email Configuration (Gmail example)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# File Upload Configuration
app.upload.dir=./uploads
app.upload.max-file-size=5MB
```

5. **Install Dependencies and Build**
```bash
mvn clean install
```

6. **Run Database Migrations**
```bash
mvn flyway:migrate
```

7. **Start the Application**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The application will start on `http://localhost:8080`

### Verification

1. **Health Check**: Visit `http://localhost:8080/actuator/health`
2. **API Documentation**: Visit `http://localhost:8080/swagger-ui.html`
3. **WebSocket Test**: Open `complete-caro-game-interface.html` in your browser

## Configuration

### Application Profiles

The application supports multiple profiles for different environments:

- **default**: Base configuration
- **local**: Local development environment
- **dev**: Development server environment
- **staging**: Staging environment for testing
- **prod**: Production environment

### Key Configuration Properties

#### Database Configuration
```properties
# Primary Database
spring.datasource.url=jdbc:mysql://localhost:3306/caro_game_db
spring.datasource.username=${DB_USERNAME:caro_user}
spring.datasource.password=${DB_PASSWORD:secure_password}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Connection Pool Configuration
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
```

#### Redis Configuration
```properties
# Redis Server
spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}
spring.data.redis.password=${REDIS_PASSWORD:}
spring.data.redis.timeout=2000ms

# Redis Connection Pool
spring.data.redis.lettuce.pool.max-active=8
spring.data.redis.lettuce.pool.max-idle=8
spring.data.redis.lettuce.pool.min-idle=0
```

#### Security Configuration
```properties
# JWT Settings
jwt.secret=${JWT_SECRET:your-secret-key}
jwt.access-token-expiration=${JWT_ACCESS_EXPIRATION:86400000}
jwt.refresh-token-expiration=${JWT_REFRESH_EXPIRATION:604800000}

# CORS Settings
app.cors.allowed-origins=${CORS_ORIGINS:http://localhost:3000,http://localhost:8080}
app.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
app.cors.allowed-headers=*
app.cors.allow-credentials=true
```

#### Email Configuration
```properties
# SMTP Configuration
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME:your-email@gmail.com}
spring.mail.password=${MAIL_PASSWORD:your-app-password}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Email Templates
app.mail.from=${MAIL_FROM:noreply@carogame.com}
app.mail.base-url=${APP_BASE_URL:http://localhost:8080}
```

#### File Upload Configuration
```properties
# File Upload Settings
spring.servlet.multipart.max-file-size=${MAX_FILE_SIZE:5MB}
spring.servlet.multipart.max-request-size=${MAX_REQUEST_SIZE:10MB}
app.upload.dir=${UPLOAD_DIR:./uploads}
app.upload.avatar.max-size=${AVATAR_MAX_SIZE:2MB}
app.upload.avatar.allowed-types=image/jpeg,image/png,image/gif
```

#### WebSocket Configuration
```properties
# WebSocket Settings
app.websocket.allowed-origins=${WS_ORIGINS:http://localhost:3000,http://localhost:8080}
app.websocket.heartbeat-interval=${WS_HEARTBEAT:10000}
app.websocket.connection-timeout=${WS_TIMEOUT:30000}
```

### Environment Variables

For production deployment, use environment variables:

```bash
# Database
export DB_HOST=your-db-host
export DB_PORT=3306
export DB_NAME=caro_game_db
export DB_USERNAME=caro_user
export DB_PASSWORD=secure_password

# Redis
export REDIS_HOST=your-redis-host
export REDIS_PORT=6379
export REDIS_PASSWORD=redis_password

# Security
export JWT_SECRET=your-256-bit-secret-key
export JWT_ACCESS_EXPIRATION=86400000
export JWT_REFRESH_EXPIRATION=604800000

# Email
export MAIL_HOST=smtp.gmail.com
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password

# Application
export SERVER_PORT=8080
export CORS_ORIGINS=https://yourdomain.com
export APP_BASE_URL=https://yourdomain.com
```

## Development Guide

### Project Structure

```
src/
├── main/
│   ├── java/com/vn/caro_game/
│   │   ├── configs/          # Configuration classes
│   │   ├── controllers/      # REST and WebSocket controllers
│   │   │   └── base/        # Base controller classes
│   │   ├── dtos/            # Data Transfer Objects
│   │   │   ├── request/     # Request DTOs
│   │   │   └── response/    # Response DTOs
│   │   ├── entities/        # JPA entities
│   │   ├── enums/           # Enumeration classes
│   │   ├── exceptions/      # Custom exception classes
│   │   ├── integrations/    # External service integrations
│   │   │   └── redis/       # Redis integration
│   │   ├── mappers/         # MapStruct mappers
│   │   ├── repositories/    # Data access repositories
│   │   ├── services/        # Business logic services
│   │   │   ├── impl/        # Service implementations
│   │   │   └── interfaces/  # Service interfaces
│   │   └── utils/           # Utility classes
│   └── resources/
│       ├── application.properties
│       ├── db/migration/    # Flyway migration scripts
│       └── templates/       # Email templates
└── test/                    # Test classes
    ├── java/               # Java test files
    └── resources/          # Test resources
```

### Development Workflow

1. **Feature Development**
   - Create feature branch from `develop`
   - Implement feature with tests
   - Update documentation if needed
   - Create pull request to `develop`

2. **Code Standards**
   - Follow Java naming conventions
   - Use Lombok to reduce boilerplate
   - Write comprehensive JavaDoc comments
   - Maintain high test coverage (>80%)

3. **Database Changes**
   - Create Flyway migration scripts for schema changes
   - Test migrations on local database
   - Update entity classes and repositories
   - Add integration tests for new queries

4. **API Development**
   - Follow RESTful conventions
   - Use appropriate HTTP status codes
   - Document APIs with Swagger annotations
   - Implement proper error handling

### Testing Strategy

#### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class GameServiceTest {
    
    @Mock
    private GameRepository gameRepository;
    
    @InjectMocks
    private GameServiceImpl gameService;
    
    @Test
    void shouldCreateGame() {
        // Given
        CreateGameRequest request = new CreateGameRequest();
        request.setRoomName("Test Room");
        
        // When
        GameResponse response = gameService.createGame(request);
        
        // Then
        assertThat(response.getRoomName()).isEqualTo("Test Room");
    }
}
```

#### Integration Tests
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.profiles.active=test")
class GameControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateGameRoom() {
        // Given
        CreateGameRequest request = new CreateGameRequest();
        request.setRoomName("Integration Test Room");
        
        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            "/api/rooms/create", request, ApiResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

#### WebSocket Tests
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketIntegrationTest {
    
    @Test
    void shouldConnectToWebSocket() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient());
        StompSession session = stompClient.connect("ws://localhost:8080/game-websocket", 
            new StompSessionHandlerAdapter(){}).get();
        
        assertThat(session.isConnected()).isTrue();
    }
}
```

### Common Development Tasks

#### Adding New API Endpoint

1. **Create DTO classes**:
```java
@Data
@Builder
public class CreateGameRequest {
    @NotBlank(message = "Room name is required")
    private String roomName;
    
    @Min(value = 2, message = "Minimum 2 players required")
    private int maxPlayers;
}
```

2. **Update Controller**:
```java
@PostMapping("/create")
@Operation(summary = "Create new game room")
public ResponseEntity<ApiResponse<GameResponse>> createGame(
        @Valid @RequestBody CreateGameRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
    
    GameResponse response = gameService.createGame(request, userDetails.getUserId());
    return success(response, "Game room created successfully");
}
```

3. **Implement Service**:
```java
@Transactional
public GameResponse createGame(CreateGameRequest request, Long userId) {
    GameRoom room = GameRoom.builder()
        .name(request.getRoomName())
        .maxPlayers(request.getMaxPlayers())
        .createdBy(userRepository.findById(userId).orElseThrow())
        .build();
    
    room = gameRoomRepository.save(room);
    return gameRoomMapper.toResponse(room);
}
```

#### Adding Database Migration

Create new migration file `V{version}__{description}.sql`:
```sql
-- V1.5__add_game_settings_table.sql
CREATE TABLE game_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    board_size INT DEFAULT 15,
    win_condition INT DEFAULT 5,
    time_limit INT DEFAULT 300,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES game_rooms(id)
);
```

## Testing

The application includes comprehensive testing at multiple levels:

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=GameServiceTest

# Run tests with coverage report
mvn test jacoco:report

# Run integration tests only
mvn test -Dtest=**/*IntegrationTest

# Run tests with specific profile
mvn test -Dspring.profiles.active=test
```

### Test Configuration

Test-specific configuration in `application-test.properties`:
```properties
# Use H2 in-memory database for tests
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop

# Disable Redis for unit tests
spring.data.redis.host=localhost
spring.data.redis.port=6370

# Fast JWT tokens for testing
jwt.access-token-expiration=60000
jwt.refresh-token-expiration=120000
```

### Test Categories

1. **Unit Tests**: Test individual components in isolation
2. **Integration Tests**: Test component interactions
3. **API Tests**: Test REST endpoints end-to-end
4. **WebSocket Tests**: Test real-time communication
5. **Repository Tests**: Test data access layer
6. **Security Tests**: Test authentication and authorization

### Test Data Management

Use `@Sql` annotation for test data:
```java
@Test
@Sql("/test-data/users.sql")
void shouldFindUserByEmail() {
    User user = userRepository.findByEmail("test@example.com");
    assertThat(user).isNotNull();
}
```

Test data file `test-data/users.sql`:
```sql
INSERT INTO users (username, email, password_hash, display_name) 
VALUES ('testuser', 'test@example.com', '$2a$10$hash', 'Test User');
```

## Deployment

### Production Deployment with Docker

1. **Create Dockerfile**:
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/caro-game-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

2. **Create docker-compose.yml**:
```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=mysql
      - REDIS_HOST=redis
    depends_on:
      - mysql
      - redis

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: caro_game_db
      MYSQL_USER: caro_user
      MYSQL_PASSWORD: secure_password
      MYSQL_ROOT_PASSWORD: root_password
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:6.2-alpine
    volumes:
      - redis_data:/data

volumes:
  mysql_data:
  redis_data:
```

3. **Deploy**:
```bash
# Build application
mvn clean package -DskipTests

# Start services
docker-compose up -d

# Check logs
docker-compose logs -f app
```

### Production Configuration

Create `application-prod.properties`:
```properties
# Security
server.port=8080
server.ssl.enabled=false

# Database with connection pooling
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.maximum-pool-size=50

# Logging
logging.level.com.vn.caro_game=INFO
logging.file.name=/var/log/caro-game/application.log

# Actuator for monitoring
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized

# Performance optimization
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.hibernate.ddl-auto=validate
```

### Monitoring and Health Checks

The application includes health check endpoints:
- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

### Backup and Recovery

1. **Database Backup**:
```bash
# Create backup
mysqldump -u caro_user -p caro_game_db > backup_$(date +%Y%m%d_%H%M%S).sql

# Restore from backup
mysql -u caro_user -p caro_game_db < backup_20231201_120000.sql
```

2. **Redis Backup**:
```bash
# Create Redis snapshot
redis-cli BGSAVE

# Copy RDB file
cp /var/lib/redis/dump.rdb /backup/redis_$(date +%Y%m%d_%H%M%S).rdb
```

## Contributing

We welcome contributions to the Caro Game project. Please follow these guidelines:

### Development Process

1. **Fork the Repository**
   - Fork the project on GitHub
   - Clone your fork locally
   - Add upstream remote: `git remote add upstream https://github.com/MinhAnh-IT/Caro-Game.git`

2. **Create Feature Branch**
   ```bash
   git checkout develop
   git pull upstream develop
   git checkout -b feature/your-feature-name
   ```

3. **Make Changes**
   - Write clean, well-documented code
   - Add tests for new functionality
   - Update documentation as needed
   - Follow existing code style and conventions

4. **Test Your Changes**
   ```bash
   mvn test
   mvn spring-boot:run
   ```

5. **Commit and Push**
   ```bash
   git add .
   git commit -m "Add: description of your changes"
   git push origin feature/your-feature-name
   ```

6. **Create Pull Request**
   - Create PR against `develop` branch
   - Provide clear description of changes
   - Include screenshots for UI changes
   - Ensure all tests pass

### Code Style Guidelines

- Use Java naming conventions (camelCase for variables, PascalCase for classes)
- Write comprehensive JavaDoc for public methods and classes
- Keep methods small and focused (preferably under 20 lines)
- Use meaningful variable and method names
- Follow SOLID principles and clean code practices
- Use Lombok annotations to reduce boilerplate code

### Commit Message Format

```
<type>: <description>

<body>

<footer>
```

Types:
- `Add`: New feature or functionality
- `Fix`: Bug fix
- `Update`: Changes to existing functionality
- `Remove`: Deletion of features or code
- `Refactor`: Code restructuring without functionality changes
- `Test`: Adding or updating tests
- `Docs`: Documentation changes

Example:
```
Add: WebSocket connection management for game rooms

- Implement automatic reconnection logic
- Add connection status indicators
- Handle network interruption gracefully

Closes #123
```

### Issue Reporting

When reporting issues, please include:
- Clear description of the problem
- Steps to reproduce the issue
- Expected vs actual behavior
- Environment details (OS, Java version, browser)
- Log messages or error traces
- Screenshots for UI issues

### Security Issues

For security-related issues, please email directly to the maintainers rather than creating public issues.

---

## License

This project is licensed under the MIT License. See LICENSE file for details.

## Support

For questions, issues, or contributions:
- Create an issue on GitHub
- Contact the development team
- Check the documentation and API reference

---

**Built with passion for multiplayer gaming experiences**
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

### WebSocket Architecture
```
Client ←→ STOMP ←→ Spring WebSocket ←→ SimpMessagingTemplate ←→ Redis Pub/Sub
```

### Database Architecture
- **PostgreSQL** cho persistent data
- **Redis** cho session management, caching, và real-time features
- **Connection pooling** với HikariCP
- **Database migration** với comprehensive schema updates

---

## Công Nghệ Sử Dụng

### Backend Technologies
- **Spring Boot 3.5.3** - Main framework
- **Spring Security 6** - Authentication & authorization
- **Spring Data JPA** - Database abstraction
- **Spring WebSocket** - Real-time communication
- **Redis** - Caching & session management
- **PostgreSQL 14+** - Primary database
- **MapStruct** - Object mapping
- **Swagger/OpenAPI 3.0** - API documentation

### Development Tools
- **Java 17** - Programming language
- **Maven** - Build tool
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **Docker** - Containerization
- **Git** - Version control

### Key Dependencies
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

## Cài Đặt & Chạy

### Yêu Cầu Hệ Thống
- **Java 17+**
- **PostgreSQL 14+**
- **Redis 7+**
- **Maven 3.8+**

### Quick Start

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

## API Documentation

### Authentication APIs
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | User registration |  |
| POST | `/api/auth/login` | User login |  |
| POST | `/api/auth/refresh-token` | Refresh JWT token |  |
| POST | `/api/auth/logout` | User logout |  |
| POST | `/api/auth/forgot-password` | Request password reset OTP |  |
| POST | `/api/auth/reset-password` | Reset password with OTP |  |
| POST | `/api/auth/request-change-password-otp` | Request change password OTP |  |
| POST | `/api/auth/change-password` | Change password with OTP |  |

### User Management APIs
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/users/profile` | Get user profile |  |
| PUT | `/api/users/profile` | Update user profile |  |
| POST | `/api/users/upload-avatar` | Upload user avatar |  |
| GET | `/api/users/search` | Search users |  |

### Friend Management APIs
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/friends` | Get friends list |  |
| POST | `/api/friends/request` | Send friend request |  |
| POST | `/api/friends/accept/{requestId}` | Accept friend request |  |
| POST | `/api/friends/decline/{requestId}` | Decline friend request |  |
| DELETE | `/api/friends/{friendId}` | Remove friend |  |
| POST | `/api/friends/block/{userId}` | Block user |  |
| POST | `/api/friends/unblock/{userId}` | Unblock user |  |

### Game Room APIs
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/rooms` | Get public rooms (paginated) |  |
| POST | `/api/rooms` | Create new room |  |
| GET | `/api/rooms/{roomId}` | Get room details |  |
| POST | `/api/rooms/{roomId}/join` | Join room by ID |  |
| POST | `/api/rooms/join-by-code` | Join room by code |  |
| POST | `/api/rooms/{roomId}/leave` | Leave room |  |
| POST | `/api/rooms/quick-play` | Quick play matchmaking |  |
| GET | `/api/rooms/current` | Get current user room |  |
| GET | `/api/rooms/history` | Get user game history |  |

### Chat APIs
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/rooms/{roomId}/chat` | Send chat message |  |

---

## 🔌 **WebSocket Endpoints**

### Connection & Authentication
```javascript
// WebSocket connection với JWT authentication
const socket = new WebSocket('ws://localhost:8080/ws?token=Bearer ' + jwtToken);
const stompClient = Stomp.over(socket);
```

### STOMP Destinations

#### Client → Server (Send)
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

#### Server → Client (Subscribe)
| Topic | Description | Event Types |
|-------|-------------|-------------|
| `/topic/room/{roomId}` | Room events | `ROOM_UPDATE`, `PLAYER_JOINED`, `PLAYER_LEFT`, `PLAYER_READY`, `GAME_STARTED`, `GAME_ENDED`, `REMATCH_REQUESTED`, `REMATCH_ACCEPTED`, `REMATCH_CREATED` |
| `/topic/room/{roomId}/moves` | Game moves | `MOVE_MADE`, `GAME_WON`, `GAME_DRAW` |
| `/topic/room/{roomId}/chat` | Chat messages | `CHAT_MESSAGE` |
| `/user/queue/notifications` | Personal notifications | `FRIEND_REQUEST`, `GAME_INVITATION` |

### Enhanced WebSocket Events

#### 2-Step Ready System Events:
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

#### 2-Step Rematch System Events:
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

## Game Features

### Enhanced Game Flow

#### 2-Step Ready System:
1. **Room Creation/Join**: Player tham gia phòng với trạng thái `NOT_READY`
2. **Mark Ready**: Player click "Mark Ready" → trạng thái `READY`
3. **Auto Game Start**: Khi cả 2 players đều `READY` → game tự động bắt đầu
4. **Game Progress**: Players chơi game với trạng thái `IN_GAME`

#### 2-Step Rematch Process:
1. **Game End**: Game kết thúc → hiển thị "Request Rematch" button
2. **Request Phase**: Player 1 click "Request Rematch" → trạng thái `REQUESTED`
3. **Accept Phase**: Player 2 thấy "Accept Rematch" button → click accept
4. **Room Creation**: Tự động tạo room mới → cả 2 players được redirect

### Game Rules & Logic
- **15x15 board** Caro game (có thể cấu hình)
- **5 in a row** to win (horizontal, vertical, diagonal)
- **Turn-based** gameplay với time limit
- **Real-time synchronization** cho tất cả moves
- **Anti-cheat** validation ở server-side

### Game States & Transitions
```
WAITING_FOR_PLAYERS → WAITING_FOR_READY → IN_PROGRESS → FINISHED
                   ↓                    ↓              ↓
                PLAYER_JOIN        BOTH_READY      GAME_END
```

### Game End Conditions
- **WIN**: 5 in a row achieved
- **DRAW**: Board full without winner
- **SURRENDER**: Player surrenders
- **LEAVE**: Player leaves during game
- **TIMEOUT**: Time limit exceeded

---

## Database Schema

### Core Tables

#### users
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

#### game_rooms
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

#### room_players
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

#### game_history
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

### Social Features Tables

#### friends
```sql
CREATE TABLE friends (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    friend_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, friend_id)
);
```

#### friend_requests
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

### Performance Indexes
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

## Testing

### Test Coverage
- **410+ Total Tests** across all layers
- **Unit Tests**: Service, Repository, Controller layers
- **Integration Tests**: WebSocket, Database, API endpoints
- **Security Tests**: Authentication, Authorization, JWT
- **Performance Tests**: Load testing, Concurrency

### Test Structure
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

### Running Tests
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

### Test Results Summary
```
 AuthControllerTest: 4/4 PASSED
 GameRoomServiceTest: 27/27 PASSED
 UserMapperTest: 17/17 PASSED  
 GameMatchTest: 11/11 PASSED
 Entity Tests: 100% PASSED
 Security Tests: 100% PASSED
 WebSocket Tests: 100% PASSED
```

---

## Performance & Monitoring

### Performance Optimizations
- **Database Connection Pooling** với HikariCP
- **Redis Caching** cho session và frequently accessed data
- **Lazy Loading** cho JPA relationships
- **Pagination** cho large datasets
- **Index Optimization** cho query performance

### Monitoring & Metrics
- **Spring Boot Actuator** cho health checks
- **Application metrics** với Micrometer
- **Database performance** monitoring
- **Redis performance** tracking
- **WebSocket connection** monitoring

### Scalability Features
- **Stateless architecture** cho horizontal scaling
- **Redis pub/sub** cho multi-instance communication
- **Load balancer ready** configuration
- **Database optimization** với proper indexing
- **Connection pooling** cho database efficiency

---

## Configuration

### Security Configuration
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

### Database Configuration
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

### Redis Configuration
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

### Email Configuration
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

## Deployment

### Docker Deployment

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

### Cloud Deployment
- **AWS**: EC2, RDS, ElastiCache
- **Google Cloud**: Compute Engine, Cloud SQL, Memorystore
- **Azure**: App Service, Azure Database, Azure Cache
- **Heroku**: Với PostgreSQL và Redis add-ons

---

## Development Guide

### Development Setup
```bash
# Clone và setup
git clone https://github.com/MinhAnh-IT/Caro-Game.git
cd Caro-Game

# Install dependencies
./mvnw clean install

# Run in development mode
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Git Workflow
```bash
# Feature development
git checkout -b feature/new-feature
git add .
git commit -m "feat: add new feature"
git push origin feature/new-feature

# Create Pull Request → Review → Merge
```

### Code Standards
- **Java Code Style**: Google Java Style Guide
- **Naming Convention**: camelCase for variables, PascalCase for classes
- **Documentation**: Javadoc for public methods
- **Testing**: Minimum 80% code coverage
- **Commit Messages**: Conventional Commits format

### Debugging
```yaml
# Debug configuration
logging:
  level:
    com.vn.caro_game: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web.socket: DEBUG
```

---

## Contributing

### Contribution Guidelines
1. **Fork** the repository
2. **Create feature branch** from `develop`
3. **Write tests** for new features
4. **Follow code standards** và conventions
5. **Submit pull request** với detailed description

### Bug Reports
- Sử dụng GitHub Issues template
- Cung cấp steps to reproduce
- Bao gồm error logs và screenshots
- Specify environment details

### Feature Requests
- Describe use case và expected behavior
- Explain why feature is needed
- Consider implementation complexity
- Provide mockups nếu applicable

---

## Support & Contact

### Contact Information
- **Author**: MinhAnh-IT
- **Email**: hma2004.it@gmail.com
- **GitHub**: [MinhAnh-IT](https://github.com/MinhAnh-IT)

### Links
- **Repository**: [Caro-Game](https://github.com/MinhAnh-IT/Caro-Game)
- **Issues**: [GitHub Issues](https://github.com/MinhAnh-IT/Caro-Game/issues)
- **API Docs**: http://localhost:8080/swagger-ui.html (when running)
- **WebSocket Test**: http://localhost:8080/test-gameroom-websocket.html

---

## License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

<div align="center">
  <h3>Thank you for using Caro Game!</h3>
  <p><em>Happy Gaming!</em></p>
  
  Don't forget to give us a star if you like this project!
</div>
