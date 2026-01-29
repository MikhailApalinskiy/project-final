package com.finalProjectLedZeppelin.auth.repo;

import com.finalProjectLedZeppelin.auth.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities.
 * <p>
 * Provides CRUD operations and custom query methods
 * for accessing user data.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Retrieves a user by email address.
     *
     * @param email user's email address
     * @return an {@link Optional} containing the user if found,
     * or empty if no user exists with the given email
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds up to 20 users whose email contains the given query,
     * ignoring case, ordered alphabetically by email.
     *
     * @param q search query to match against user emails
     * @return list of matching users, limited to 20 results
     */
    List<User> findTop20ByEmailContainingIgnoreCaseOrderByEmailAsc(String q);

    /**
     * Finds users whose email contains the given query, ignoring case,
     * and returns the result in a paginated form.
     *
     * @param q        search query to match against user emails
     * @param pageable pagination information
     * @return a page of matching users
     */
    Page<User> findByEmailContainingIgnoreCase(String q, Pageable pageable);

    /**
     * Checks whether a user exists with the given email address.
     *
     * @param email user's email address
     * @return {@code true} if a user with the given email exists,
     * {@code false} otherwise
     */
    boolean existsByEmail(String email);
}
