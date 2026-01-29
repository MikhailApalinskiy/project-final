package com.finalProjectLedZeppelin.auth.dto;

/**
 * Lightweight representation of a user option.
 * <p>
 * Typically used for selection lists, search results,
 * or autocomplete features.
 *
 * @param id    unique identifier of the user
 * @param email user's email address
 */
public record UserOption(Long id, String email) {
}
