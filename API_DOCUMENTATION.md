# 🎮 Caro Game API Documentation

## 🎯 Game Features Overview

This Caro Game API provides comprehensive features for online multiplayer Caro gaming with real-time communication and friend management.

## 🚀 Core Features

### 🔐 Authentication System
- JWT-based authentication with access & refresh tokens
- Username-based login system
- Secure OTP verification for password management
- Account management with proper session handling

### 👥 User Management
- User registration and profile management
- Friend system with status tracking (pending, accepted, blocked)
- Online status monitoring with Redis TTL
- User presence tracking across game sessions

### 🎲 Game Management
- Real-time multiplayer Caro game rooms
- Game match tracking with move history
- Player vs Player gameplay with turn management
- Game statistics and match results

### 💬 Real-time Communication
- WebSocket-based real-time messaging
- In-game chat functionality
- Live game state updates
- Online status broadcasting

### 📊 Data Persistence
- PostgreSQL for persistent game data
- Redis for caching and real-time features
- Comprehensive audit trails
- Optimized query performance

## 🌐 WebSocket Endpoints

### Connection
- **Endpoint**: `/ws`
- **Protocol**: STOMP over WebSocket
- **Authentication**: JWT token in handshake

### Message Destinations

#### 📬 Subscriptions (Client subscribes to)
```
/topic/user-status     # Global user status updates
/topic/game/{roomId}   # Game room specific updates
/user/queue/messages   # Private user messages
```

#### 📤 Send Destinations (Client sends to)
```
/app/game/move         # Send game move
/app/chat/send         # Send chat message
/app/status/update     # Update user status
```

## 🎮 Game Flow

### 1. User Authentication
```
POST /api/auth/login
→ Receive JWT tokens
→ Use access token for API calls
```

### 2. Friend Management
```
GET /api/friends              # Get friends list
POST /api/friends/request     # Send friend request
PUT /api/friends/accept       # Accept request
```

### 3. Game Room Creation
```
POST /api/rooms               # Create game room
GET /api/rooms                # List available rooms
POST /api/rooms/{id}/join     # Join specific room
```

### 4. Real-time Gaming
```
WebSocket: /ws
→ Subscribe to /topic/game/{roomId}
→ Send moves via /app/game/move
→ Receive game updates in real-time
```

## 📡 API Response Format

### Success Response
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    // Response data here
  },
  "timestamp": "2025-01-26T15:30:00Z"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "error": {
    "code": "ERROR_CODE",
    "details": "Detailed error information"
  },
  "timestamp": "2025-01-26T15:30:00Z"
}
```

## 🔧 Redis Data Structure

### User Online Status
```
Key: user:status:{userId}
Value: "online" | "offline" | "in-game"
TTL: 300 seconds (5 minutes)
```

### OTP Storage
```
Key: otp:{email}:{type}
Value: {otp_code}
TTL: 600 seconds (10 minutes)
```

### Game Sessions
```
Key: game:session:{roomId}
Value: JSON game state
TTL: 3600 seconds (1 hour)
```

## 🎯 Game Rules

### Caro Game Logic
- **Board Size**: 15x15 grid
- **Win Condition**: 5 consecutive pieces (horizontal, vertical, or diagonal)
- **Players**: 2 players (X and O)
- **Turn System**: Alternating turns starting with X

### Move Validation
- Position must be empty
- Must be player's turn
- Position must be within board bounds
- Game must be in active state

## 🔒 Security Features

### Authentication
- JWT tokens with configurable expiration
- Refresh token rotation
- Secure password hashing with BCrypt

### Authorization
- Role-based access control
- Resource-level permissions
- Rate limiting on sensitive endpoints

### Data Protection
- Input validation and sanitization
- SQL injection prevention
- XSS protection headers
- CORS policy configuration

## 📊 Performance Optimizations

### Database
- Indexed foreign keys
- Optimized JPA queries
- Connection pooling with HikariCP
- Database-specific optimizations

### Caching
- Redis caching for frequently accessed data
- TTL-based cache invalidation
- Optimized cache key strategies

### Real-time Features
- Efficient WebSocket connection management
- Message broadcasting optimization
- Connection pooling for scalability

## 🧪 Testing Strategy

### Unit Tests
- Service layer testing with Mockito
- Controller testing with MockMvc
- Entity validation testing
- Repository testing with @DataJpaTest

### Integration Tests
- WebSocket integration testing
- Database integration with Testcontainers
- Redis integration testing
- End-to-end API testing

### Test Coverage
- **Controllers**: 100% method coverage
- **Services**: 95%+ line coverage
- **Entities**: Complete validation testing
- **Repositories**: Query method testing

## 📈 Monitoring & Logging

### Application Metrics
- Request/response times
- Active WebSocket connections
- Database query performance
- Redis cache hit rates

### Health Checks
- Database connectivity
- Redis availability
- External service status
- Application health endpoints

## 🚀 Deployment

### Production Environment
```bash
# Build application
mvn clean package -DskipTests

# Run with production profile
java -jar target/caro-game-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Docker Deployment
```dockerfile
FROM openjdk:17-jre-slim
COPY target/caro-game-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🔄 API Versioning

The API follows semantic versioning principles:
- **Major version**: Breaking changes
- **Minor version**: New features (backward compatible)
- **Patch version**: Bug fixes

Current version: `v1.0.0`

## 📞 Support & Contact

- **GitHub Issues**: https://github.com/MinhAnh-IT/Caro-Game/issues
- **Documentation**: Available in Swagger UI
- **Email Support**: dev@carogame.com

---

*This documentation is auto-generated and maintained alongside the codebase to ensure accuracy and completeness.*
