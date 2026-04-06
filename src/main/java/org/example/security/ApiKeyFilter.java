package org.example.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;


public class ApiKeyFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    @Value("${app.max.usage.limit}")
    private int maxUsageLimit;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // 1. Skip check for the registration endpoint
        if (uri.equals("/api/v1/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestApiKey = request.getHeader("x-api-key");
        if (requestApiKey == null) {
            requestApiKey = request.getHeader("X-API-KEY");
        }


        // Use the repository to find the user
        var userOptional = userRepository.findByApiKey(requestApiKey);

        if (userOptional.isPresent()) {
            var user = userOptional.get();

            // --- NEW: USAGE LIMIT CHECK -- Limiting to prevent crashes. ---
            if (user.getRequestCount() >= maxUsageLimit) {
                response.setStatus(429); // 429 is the standard for Too Many Requests
                response.setHeader("Content-Type", "application/json");
                response.getWriter().write("{\"error\": \"Usage limit exceeded. You have used " + maxUsageLimit + " requests.\"}");
                return; // Stop the request here
            }
            // -----------------------------

            // 1. Increment the count
            // Update request count (Scalable thinking!)
            user.setRequestCount(user.getRequestCount() + 1);
            // 2. Save the update to the database
            userRepository.save(user);

            // Log the success
            System.out.println("Authenticated user: " + user.getEmail());

            // 3. Log it for your own monitoring
            System.out.println("User " + user.getEmail() + " performed request #" + user.getRequestCount());

            var auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid API Key. Please register first at /api/v1/register");
        }
    }
}
