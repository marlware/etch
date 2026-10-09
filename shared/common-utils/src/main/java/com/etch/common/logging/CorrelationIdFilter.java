package com.etch.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Reads {@code X-Correlation-Id} from the incoming request (set by the API
 * Gateway, or by an upstream service when one hop calls another) and puts
 * it in the SLF4J MDC so every log line for the request can be tied
 * together, then echoes it back on the response. Values that don't look
 * like an id (too long, odd characters) are replaced with a fresh UUID so
 * a client can't push arbitrary text into the logs.
 */
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Pattern VALID_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = request.getHeader(CorrelationIdConstants.HEADER_NAME);
        if (!StringUtils.hasText(correlationId) || !VALID_ID.matcher(correlationId).matches()) {
            correlationId = UUID.randomUUID().toString();
        }
        try {
            MDC.put(CorrelationIdConstants.MDC_KEY, correlationId);
            response.setHeader(CorrelationIdConstants.HEADER_NAME, correlationId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CorrelationIdConstants.MDC_KEY);
        }
    }
}
