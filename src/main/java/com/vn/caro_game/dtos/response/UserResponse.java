package com.vn.caro_game.dtos.response;

import com.vn.caro_game.enums.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long id;
    String username;
    String email;
    String avatarUrl;
    UserStatus status;
    LocalDateTime createdAt;
}
