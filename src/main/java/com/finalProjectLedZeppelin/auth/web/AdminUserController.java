package com.finalProjectLedZeppelin.auth.web;

import com.finalProjectLedZeppelin.auth.dto.UpdateUserRoleRequest;
import com.finalProjectLedZeppelin.auth.dto.UserAdminResponse;
import com.finalProjectLedZeppelin.auth.model.User;
import com.finalProjectLedZeppelin.auth.model.UserRole;
import com.finalProjectLedZeppelin.auth.repo.UserRepository;
import com.finalProjectLedZeppelin.common.error.NotFoundException;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller providing administrative operations for managing users.
 * <p>
 * Exposes endpoints for listing users, updating user roles,
 * and deleting user accounts. Access is restricted to users
 * with the {@code ADMIN} role.
 */
@Log4j2
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;

    /**
     * Creates a new {@code AdminUserController} instance.
     *
     * @param userRepository repository used to access and manage users
     */
    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns a paginated list of users.
     * <p>
     * Supports optional case-insensitive search by email.
     *
     * @param q        optional search query for filtering users by email
     * @param pageable pagination and sorting information
     * @return page of users represented as {@link UserAdminResponse}
     */
    @GetMapping
    public Page<UserAdminResponse> list(
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        log.debug(
                "Admin users list requested (query={}, page={}, size={}, sort={})",
                q,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
        );
        Page<User> page = (q == null || q.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository.findByEmailContainingIgnoreCase(q.trim(), pageable);
        log.debug(
                "Admin users list returned (totalElements={})",
                page.getTotalElements()
        );
        return page.map(u -> new UserAdminResponse(
                u.getId(),
                u.getEmail(),
                u.getRole().name(),
                u.getCreatedAt()
        ));
    }

    /**
     * Updates the role of a specific user.
     *
     * @param id  identifier of the target user
     * @param req request containing the new role
     * @return updated user representation
     * @throws NotFoundException        if the user does not exist
     * @throws IllegalArgumentException if the provided role is invalid
     */
    @PatchMapping("/{id}/role")
    public UserAdminResponse updateRole(@PathVariable Long id, @RequestBody @Valid UpdateUserRoleRequest req) {
        log.info(
                "Admin role update requested (targetUserId={}, newRole={})",
                id, req.role()
        );
        User u = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "Admin role update failed: user not found (targetUserId={})",
                            id
                    );
                    return new NotFoundException("User not found");
                });
        u.setRole(UserRole.valueOf(req.role()));
        log.info(
                "Admin role updated (targetUserId={}, role={})",
                u.getId(), u.getRole()
        );
        return new UserAdminResponse(u.getId(), u.getEmail(), u.getRole().name(), u.getCreatedAt());
    }

    /**
     * Deletes a user account.
     * <p>
     * Self-deletion by an administrator is not allowed.
     *
     * @param id   identifier of the target user
     * @param auth authentication object representing the current admin
     * @throws IllegalArgumentException if an admin attempts to delete themselves
     * @throws NotFoundException        if the user does not exist
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        Long currentUserId = (Long) auth.getPrincipal();
        log.info(
                "Admin delete requested (targetUserId={}, adminId={})",
                id, currentUserId
        );
        if (id.equals(currentUserId)) {
            log.warn(
                    "Admin delete rejected: self-delete attempt (adminId={})",
                    currentUserId
            );
            throw new IllegalArgumentException("You can't delete yourself");
        }
        if (!userRepository.existsById(id)) {
            log.warn(
                    "Admin delete failed: user not found (targetUserId={})",
                    id
            );
            throw new NotFoundException("User not found");
        }
        userRepository.deleteById(id);
        log.info(
                "Admin delete success (targetUserId={}, adminId={})",
                id, currentUserId
        );
    }
}
