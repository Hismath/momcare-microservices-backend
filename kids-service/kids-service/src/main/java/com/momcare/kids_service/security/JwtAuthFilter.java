package com.momcare.kids_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Extract Authorization header
        final String authHeader = request.getHeader("Authorization");
        String email = null;
        String jwt = null;

        // Check if the Authorization header is present and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); // Extract token

            try {
                // Extract email from token
                email = jwtUtil.extractEmail(jwt);
            } catch (ExpiredJwtException e) {
                logger.error("Expired JWT token", e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Expired JWT token");
                return;
            } catch (JwtException e) {
                logger.error("Invalid JWT token", e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                return;
            }
        }

        // If the email is valid and token is not expired, set the authentication
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (!jwtUtil.isTokenExpired(jwt)) {
                // Create authentication token
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(email, null, null);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set the authentication object in the context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                logger.error("JWT token is expired");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT token is expired");
                return;
            }
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}
