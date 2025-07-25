package com.vn.caro_game.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Enums Tests")
class EnumsTest {

    @Test
    @DisplayName("Should have correct UserStatus values")
    void shouldHaveCorrectUserStatusValues() {
        assertThat(UserStatus.values()).hasSize(2);
        assertThat(UserStatus.ONLINE).isNotNull();
        assertThat(UserStatus.OFFLINE).isNotNull();
        
        assertThat(UserStatus.ONLINE.name()).isEqualTo("ONLINE");
        assertThat(UserStatus.OFFLINE.name()).isEqualTo("OFFLINE");
    }

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
    }

    @Test
    @DisplayName("Should have UserStatus enum with correct string representation")
    void shouldHaveUserStatusEnumWithCorrectStringRepresentation() {
        assertThat(UserStatus.ONLINE.toString()).isEqualTo("ONLINE");
        assertThat(UserStatus.OFFLINE.toString()).isEqualTo("OFFLINE");
    }

    @Test
    @DisplayName("Should be able to get StatusCode values by name")
    void shouldBeAbleToGetStatusCodeValuesByName() {
        assertThat(StatusCode.valueOf("EMAIL_ALREADY_EXISTS")).isEqualTo(StatusCode.EMAIL_ALREADY_EXISTS);
        assertThat(StatusCode.valueOf("INVALID_CREDENTIALS")).isEqualTo(StatusCode.INVALID_CREDENTIALS);
        assertThat(StatusCode.valueOf("USER_NOT_FOUND")).isEqualTo(StatusCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("Should be able to get UserStatus values by name")
    void shouldBeAbleToGetUserStatusValuesByName() {
        assertThat(UserStatus.valueOf("ONLINE")).isEqualTo(UserStatus.ONLINE);
        assertThat(UserStatus.valueOf("OFFLINE")).isEqualTo(UserStatus.OFFLINE);
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
    @DisplayName("Should have distinct UserStatus values")
    void shouldHaveDistinctUserStatusValues() {
        UserStatus[] values = UserStatus.values();
        assertThat(values).doesNotHaveDuplicates();
    }
}
