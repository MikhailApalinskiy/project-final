package com.finalProjectLedZeppelin.auth.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request used to update a user's role.
 *
 * @param role new role to be assigned to the user; must not be null
 */
public record UpdateUserRoleRequest(@NotNull String role) {
}
