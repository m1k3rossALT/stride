package com.tracker.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Runs first on every request (Order 1).
 *
 * What it does:
 *   1. Reads X-Request-ID from the incoming request header (useful for tracing from a gateway)
 *   2. If none present, generates a short random ID
 *   3. Puts it into MDC so every log line in this request includes [req-id:xxxx]
 *   4. Echoes the ID back on the response header
 *   5. Logs the inbound request and outbound response (method, path, status, duration)
 *   6. Always clears MDC after the request — prevents context leaking across threads
 *
 * Verbose mode: set LOG_LEVEL_APP=DEBUG to see additional per-request details.
 */
@Slf4j
@Component
@Order(1)
public class MdcRequestFilter extends OncePerRequestFilter {

    private static final String MDC_REQUEST_ID_KEY  = "requestId";
    private static final String REQUEST_ID_HEADER   = "X-Request-ID";

    @Override
    protected void doFilterInternal(
            HttpServletRequest  request,
            HttpServletResponse response,
            FilterChain         filterChain
    ) throws ServletException, IOException {

        String requestId = resolveRequestId(request);
        MDC.put(MDC_REQUEST_ID_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        long startTime = System.currentTimeMillis();

        try {
            log.info("→ {} {}",  request.getMethod(), request.getRequestURI());

            // Verbose: log query string if present
            if (log.isDebugEnabled() && request.getQueryString() != null) {
                log.debug("  query={}", request.getQueryString());
            }

            filterChain.doFilter(request, response);

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("← {} {}  status={}  duration_ms={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration
            );
            MDC.clear();  // critical — prevents MDC leaking into thread pool reuse
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String incoming = request.getHeader(REQUEST_ID_HEADER);
        if (incoming != null && !incoming.isBlank()) {
            return incoming;
        }
        // Short 8-char ID is readable in logs without being verbose
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
