package com.vn.caro_game.dtos.response;

import com.vn.caro_game.enums.GameEndReason;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GameHistoryResponse {
    Long id;
    Long roomId;
    String roomName;
    Long winnerId;
    String winnerName;
    String winnerAvatar;
    Long loserId;
    String loserName;
    String loserAvatar;
    GameEndReason endReason;
    LocalDateTime gameStartedAt;
    LocalDateTime gameEndedAt;
    LocalDateTime createdAt;
    
    // Additional computed fields
    Boolean isWinner; // true if current user is the winner
    String opponentName;
    String opponentAvatar;
    Long gameDurationMinutes;
}
