package com.finalProjectLedZeppelin.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that ensures every HTTP request is associated
 * with a unique request identifier.
 * <p>
 * The request ID is resolved from the {@code X-Request-Id} header
 * if present, or generated automatically otherwise. The identifier
 * is stored in the MDC to enable request-level log correlation and
 * is also returned to the client in the response headers.
 */
public class RequestIdMdcFilter extends OncePerRequestFilter {

    /**
     * HTTP header used to carry the request identifier.
     */
    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    /**
     * MDC key under which the request identifier is stored.
     */
    public static final String MDC_REQUEST_ID = "requestId";

    /**
     * Filters incoming HTTP requests and associates them with
     * a request identifier.
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
        String requestId = resolveOrGenerateRequestId(request);
        try {
            MDC.put(MDC_REQUEST_ID, requestId);
            response.setHeader(HEADER_REQUEST_ID, requestId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_REQUEST_ID);
        }
    }

    /**
     * Resolves the request identifier from the incoming request
     * or generates a new one if it is not present.
     *
     * @param request current HTTP request
     * @return existing or newly generated request identifier
     */
    private String resolveOrGenerateRequestId(HttpServletRequest request) {
        String incoming = request.getHeader(HEADER_REQUEST_ID);
        if (StringUtils.hasText(incoming)) {
            return incoming.trim();
        }
        return UUID.randomUUID().toString();
    }
}
