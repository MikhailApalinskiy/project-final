package com.finalProjectLedZeppelin.auth.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @Test
    void prePersist_shouldSetCreatedAt_whenCreatedAtIsNull() {
        // given
        User user = new User();
        user.setCreatedAt(null);
        // when
        user.prePersist();
        // then
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void prePersist_shouldNotOverrideCreatedAt_whenCreatedAtAlreadySet() {
        // given
        User user = new User();
        Instant fixed = Instant.parse("2020-01-01T00:00:00Z");
        user.setCreatedAt(fixed);
        // when
        user.prePersist();
        // then
        assertEquals(fixed, user.getCreatedAt());
    }

    @Test
    void prePersist_shouldSetDefaultRole_whenRoleIsNull() {
        // given
        User user = new User();
        user.setRole(null);
        // when
        user.prePersist();
        // then
        assertEquals(UserRole.USER, user.getRole());
    }

    @Test
    void prePersist_shouldNotOverrideRole_whenRoleAlreadySet() {
        // given
        User user = new User();
        UserRole already = UserRole.USER;
        user.setRole(already);
        // when
        user.prePersist();
        // then
        assertEquals(already, user.getRole());
    }

    @Test
    void prePersist_shouldSetBothDefaults_whenBothAreNull() {
        // given
        User user = new User();
        user.setCreatedAt(null);
        user.setRole(null);
        // when
        user.prePersist();
        // then
        assertNotNull(user.getCreatedAt());
        assertEquals(UserRole.USER, user.getRole());
    }
}