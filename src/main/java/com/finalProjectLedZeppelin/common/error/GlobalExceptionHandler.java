package com.finalProjectLedZeppelin.common.error;

import com.finalProjectLedZeppelin.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * Global exception handler for REST controllers.
 * <p>
 * Converts application and framework exceptions into
 * standardized {@link ApiError} responses.
 */
@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles {@link IllegalArgumentException}.
     *
     * @param ex  thrown exception
     * @param req current HTTP request
     * @return API error response with HTTP 400 (Bad Request)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        log.warn(
                "Bad request: illegal argument (path={}, message={})",
                req.getRequestURI(),
                ex.getMessage()
        );
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    /**
     * Handles user registration conflicts caused by duplicate email addresses.
     *
     * @param ex  thrown exception
     * @param req current HTTP request
     * @return API error response with HTTP 409 (Conflict)
     */
    @ExceptionHandler(AuthService.EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailExists(AuthService.EmailAlreadyExistsException ex, HttpServletRequest req) {
        log.warn(
                "Auth register conflict: email already exists (path={}, message={})",
                req.getRequestURI(),
                ex.getMessage()
        );
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    /**
     * Handles authentication failures caused by invalid credentials.
     *
     * @param ex  thrown exception
     * @param req current HTTP request
     * @return API error response with HTTP 401 (Unauthorized)
     */
    @ExceptionHandler(AuthService.BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCred(AuthService.BadCredentialsException ex, HttpServletRequest req) {
        log.warn(
                "Auth failed: bad credentials (path={})",
                req.getRequestURI()
        );
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    /**
     * Handles validation errors for request bodies annotated with {@code @Valid}.
     *
     * @param ex  thrown exception containing validation details
     * @param req current HTTP request
     * @return API error response with HTTP 400 (Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse("Validation failed");
        log.warn(
                "Validation failed (path={}, error={})",
                req.getRequestURI(),
                msg
        );
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    /**
     * Handles database constraint and integrity violations.
     *
     * @param ex  thrown exception
     * @param req current HTTP request
     * @return API error response with HTTP 409 (Conflict)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn(
                "Data integrity violation (path={})",
                req.getRequestURI()
        );
        return build(HttpStatus.CONFLICT, "Data integrity violation", req);
    }

    /**
     * Handles malformed or unreadable HTTP request bodies (e.g. invalid JSON).
     *
     * @param ex  thrown exception
     * @param req current HTTP request
     * @return API error response with HTTP 400 (Bad Request)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        log.warn(
                "Malformed JSON request (path={})",
                req.getRequestURI()
        );
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON request", req);
    }

    /**
     * Handles cases when a requested resource cannot be found.
     *
     * @param ex  thrown exception
     * @param req current HTTP request
     * @return API error response with HTTP 404 (Not Found)
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        log.warn(
                "Resource not found (path={}, message={})",
                req.getRequestURI(),
                ex.getMessage()
        );
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    /**
     * Builds a standardized {@link ApiError} response.
     *
     * @param status  HTTP status to return
     * @param message error message
     * @param req     current HTTP request
     * @return response entity containing the API error
     */
    private static ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest req) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                req.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
