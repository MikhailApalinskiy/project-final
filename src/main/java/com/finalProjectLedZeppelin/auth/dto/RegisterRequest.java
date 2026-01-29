package com.finalProjectLedZeppelin.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registration request containing user credentials.
 * <p>
 * Used for creating a new user account. All fields are validated
 * before the registration process is executed.
 *
 * @param email    user's email address; must be valid, non-blank,
 *                 and no longer than 320 characters
 * @param password user's raw password; must be non-blank and
 *                 contain between 6 and 72 characters
 */
public record RegisterRequest(
        @Email @NotBlank @Size(max = 320) String email,
        @NotBlank @Size(min = 6, max = 72) String password
) {
}
