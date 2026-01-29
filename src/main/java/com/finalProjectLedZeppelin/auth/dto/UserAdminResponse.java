package com.finalProjectLedZeppelin.auth.dto;

import java.time.Instant;

/**
 * Administrative representation of a user.
 * <p>
 * Used in admin-level operations to expose user details
 * required for management and auditing purposes.
 *
 * @param id        unique identifier of the user
 * @param email     user's email address
 * @param role      user's assigned role
 * @param createdAt timestamp when the user account was created
 */
public record UserAdminResponse(Long id, String email, String role, Instant createdAt) {
}
