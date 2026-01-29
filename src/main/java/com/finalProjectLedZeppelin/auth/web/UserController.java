package com.finalProjectLedZeppelin.auth.web;

import com.finalProjectLedZeppelin.auth.dto.UserOption;
import com.finalProjectLedZeppelin.auth.repo.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller providing user search functionality.
 * <p>
 * Intended for administrative use, typically to support
 * user selection and autocomplete features.
 */
@Log4j2
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    /**
     * Creates a new {@code UserController} instance.
     *
     * @param userRepository repository used to search for users
     */
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Searches for users by email.
     * <p>
     * Performs a case-insensitive search and returns up to
     * 20 matching users ordered by email. If the query is empty,
     * an empty list is returned.
     *
     * @param q search query for matching user emails
     * @return list of matching users represented as {@link UserOption}
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserOption> search(@RequestParam(defaultValue = "") String q) {
        String query = q == null ? "" : q.trim();
        log.debug("Admin user search requested (query='{}')", query);
        if (query.isEmpty()) {
            log.debug("Admin user search skipped: empty query");
            return List.of();
        }
        List<UserOption> result = userRepository
                .findTop20ByEmailContainingIgnoreCaseOrderByEmailAsc(query)
                .stream()
                .map(u -> new UserOption(u.getId(), u.getEmail()))
                .toList();
        log.debug("Admin user search result (query='{}', count={})", query, result.size());
        return result;
    }
}

