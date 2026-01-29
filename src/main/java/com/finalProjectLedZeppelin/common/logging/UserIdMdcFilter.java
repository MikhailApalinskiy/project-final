package com.finalProjectLedZeppelin.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that adds the authenticated user's identifier
 * to the logging MDC.
 * <p>
 * If a request is authenticated and the security principal
 * contains a user ID, it is stored in the MDC under the {@code userId}
 * key for the duration of the request. This enables user-level
 * log correlation.
 */
public class UserIdMdcFilter extends OncePerRequestFilter {

    /**
     * MDC key under which the user identifier is stored.
     */
    public static final String MDC_USER_ID = "userId";

    /**
     * Filters incoming HTTP requests and enriches the MDC
     * with the authenticated user's identifier, if available.
     *
     * @param request     current HTTP request
     * @param response    current HTTP response
     * @param filterChain filter chain to continue processing
     * @throws ServletException in case of servlet errors
     * @throws IOException      in case of I/O errors
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Long userId) {
                MDC.put(MDC_USER_ID, String.valueOf(userId));
            }
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_USER_ID);
        }
    }
}
