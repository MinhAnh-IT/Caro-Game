package com.vn.caro_game.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Enums Tests")
class EnumsTest {

    @Test
    @DisplayName("Should have correct StatusCode values")
    void shouldHaveCorrectStatusCodeValues() {
        // Test some key status codes
        assertThat(StatusCode.EMAIL_ALREADY_EXISTS).isNotNull();
        assertThat(StatusCode.USERNAME_ALREADY_EXISTS).isNotNull();
        assertThat(StatusCode.INVALID_CREDENTIALS).isNotNull();
        assertThat(StatusCode.USER_NOT_FOUND).isNotNull();
        assertThat(StatusCode.EMAIL_NOT_FOUND).isNotNull();
        assertThat(StatusCode.INVALID_OTP).isNotNull();
        assertThat(StatusCode.OTP_ATTEMPTS_EXCEEDED).isNotNull();
        assertThat(StatusCode.CURRENT_PASSWORD_INCORRECT).isNotNull();
        assertThat(StatusCode.INVALID_REFRESH_TOKEN).isNotNull();
        assertThat(StatusCode.EMAIL_SEND_FAILED).isNotNull();
        assertThat(StatusCode.UNAUTHORIZED).isNotNull();
        assertThat(StatusCode.FORBIDDEN).isNotNull();
        assertThat(StatusCode.NOT_FOUND).isNotNull();
        assertThat(StatusCode.TOO_MANY_REQUESTS).isNotNull();
        assertThat(StatusCode.ACCOUNT_LOCKED).isNotNull();

        // Verify status codes have proper values
        assertThat(StatusCode.EMAIL_ALREADY_EXISTS.getCode()).isEqualTo(4001);
        assertThat(StatusCode.USERNAME_ALREADY_EXISTS.getCode()).isEqualTo(4002);
        assertThat(StatusCode.INVALID_CREDENTIALS.getCode()).isEqualTo(4003);
    }

    @Test
    @DisplayName("Should be able to get StatusCode values by name")
    void shouldBeAbleToGetStatusCodeValuesByName() {
        assertThat(StatusCode.valueOf("EMAIL_ALREADY_EXISTS")).isEqualTo(StatusCode.EMAIL_ALREADY_EXISTS);
        assertThat(StatusCode.valueOf("INVALID_CREDENTIALS")).isEqualTo(StatusCode.INVALID_CREDENTIALS);
        assertThat(StatusCode.valueOf("USER_NOT_FOUND")).isEqualTo(StatusCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("Should have StatusCode values count greater than 10")
    void shouldHaveStatusCodeValuesCountGreaterThan10() {
        // Ensure we have a good number of status codes defined
        assertThat(StatusCode.values().length).isGreaterThan(10);
    }

    @Test
    @DisplayName("Should have distinct StatusCode values")
    void shouldHaveDistinctStatusCodeValues() {
        StatusCode[] values = StatusCode.values();
        assertThat(values).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("RoomStatus enum should have correct values")
    void testRoomStatusEnum() {
        RoomStatus[] values = RoomStatus.values();
        
        assertThat(values).hasSize(3);
        assertThat(values).containsExactly(
            RoomStatus.WAITING,
            RoomStatus.PLAYING,
            RoomStatus.FINISHED
        );
    }

    @Test
    @DisplayName("FriendStatus enum should have correct values")
    void testFriendStatusEnum() {
        FriendStatus[] values = FriendStatus.values();
        
        assertThat(values).hasSize(3);
        assertThat(values).containsExactly(
            FriendStatus.PENDING,
            FriendStatus.ACCEPTED,
            FriendStatus.BLOCKED
        );
    }

    @Test
    @DisplayName("GameResult enum should have correct values")
    void testGameResultEnum() {
        GameResult[] values = GameResult.values();
        
        assertThat(values).hasSize(7);
        assertThat(values).containsExactly(
            GameResult.X_WIN,
            GameResult.O_WIN,
            GameResult.DRAW,
            GameResult.ONGOING,
            GameResult.WIN,
            GameResult.LOSE,
            GameResult.NONE
        );
    }
}
