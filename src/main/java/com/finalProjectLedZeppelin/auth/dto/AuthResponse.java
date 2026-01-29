package com.finalProjectLedZeppelin.auth.dto;

/**
 * Authentication response returned after a successful login or token refresh.
 *
 * @param accessToken JWT access token used to authorize subsequent API requests
 */
public record AuthResponse(String accessToken) {
}
