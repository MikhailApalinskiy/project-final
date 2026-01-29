package com.finalProjectLedZeppelin.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Authentication request containing user credentials.
 * <p>
 * Used for user login operations. All fields are validated
 * before authentication is performed.
 *
 * @param email    user's email address used as a login identifier
 * @param password user's raw password
 */
public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password
) {
}
