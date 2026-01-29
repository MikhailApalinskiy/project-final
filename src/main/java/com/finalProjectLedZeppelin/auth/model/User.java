package com.finalProjectLedZeppelin.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entity representing an application user.
 * <p>
 * Stores authentication and authorization data such as email,
 * password hash, assigned role, and account creation timestamp.
 */
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "ix_users_email", columnList = "email", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
public class User {

    /**
     * Unique identifier of the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User's email address.
     * <p>
     * Must be unique and no longer than 320 characters.
     */
    @Column(nullable = false, length = 320, unique = true)
    private String email;

    /**
     * Hashed user password.
     * <p>
     * Stores a one-way hash of the user's password
     * (e.g., bcrypt or similar algorithm).
     */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    /**
     * Role assigned to the user for authorization purposes.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    /**
     * Timestamp when the user account was created.
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * Initializes default values before the entity is persisted.
     * <p>
     * Sets the creation timestamp and default role if they are not provided.
     */
    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (role == null) {
            role = UserRole.USER;
        }
    }
}
