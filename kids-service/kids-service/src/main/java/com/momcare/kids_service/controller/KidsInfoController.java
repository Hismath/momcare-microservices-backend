package com.momcare.kids_service.controller;

import com.momcare.kids_service.model.KidsInfo;
import com.momcare.kids_service.model.UserDto;
import com.momcare.kids_service.security.JwtUtil;
import com.momcare.kids_service.service.KidsInfoServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/kids")
@CrossOrigin(origins = "*")
public class KidsInfoController {

    @Autowired
    private KidsInfoServiceImpl kidsInfoService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${user.service.url}")
    private String userServiceUrl;

    // Extract email from Authorization token
    private String extractEmail(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // Consider adding a try-catch for jwtUtil.extractEmail in case of invalid token format
            try {
                return jwtUtil.extractEmail(token);
            } catch (Exception e) {
                // Log the exception for debugging
                System.err.println("Error extracting email from JWT token: " + e.getMessage());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or malformed JWT token");
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing Authorization header");
    }

    // Get User information using email
    private UserDto getUserByEmail(String email, String token) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(userServiceUrl + "email/" + email)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    // Handle HTTP status codes directly from WebClient
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        // Corrected line for getting status information:
                        System.err.println("Error from User service: " + clientResponse.statusCode().value() + " " + clientResponse.statusCode());
                        return clientResponse.bodyToMono(String.class).flatMap(body -> {
                            if (clientResponse.statusCode() == HttpStatus.FORBIDDEN) {
                                // Specific handling for 403
                                return reactor.core.publisher.Mono.error(
                                    new ResponseStatusException(HttpStatus.FORBIDDEN, "Access to user service forbidden. " + body)
                                );
                            } else if (clientResponse.statusCode().is4xxClientError()) {
                                // General 4xx client errors
                                return reactor.core.publisher.Mono.error(
                                    new ResponseStatusException(clientResponse.statusCode(), "Client error from user service: " + body)
                                );
                            } else {
                                // General 5xx server errors
                                return reactor.core.publisher.Mono.error(
                                    new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "User service responded with an error: " + body)
                                );
                            }
                        });
                    })
                    .bodyToMono(UserDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound ex) {
            System.err.println("User not found in user service for email: " + email + ". Details: " + ex.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        } catch (WebClientResponseException.Forbidden ex) {
            System.err.println("User service returned 403 FORBIDDEN for email: " + email + ". Details: " + ex.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access to user service forbidden for this operation.");
        } catch (WebClientResponseException ex) {
            System.err.println("User service returned " + ex.getStatusCode() + " for email: " + email + ". Details: " + ex.getResponseBodyAsString());
            throw new ResponseStatusException(ex.getStatusCode(), "Error from user service: " + ex.getStatusText());
        } catch (Exception ex) {
            System.err.println("Could not connect to user service or other unexpected error: " + ex.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "User service is unavailable or an unexpected error occurred.");
        }
    }


    // Check if user is authorized to access the kids info
    private boolean isUserAuthorized(Long id, String email) {
        Optional<KidsInfo> existingOpt = kidsInfoService.getById(id);
        return existingOpt.isPresent() && existingOpt.get().getUserEmail().equals(email);
    }

    // Add KidsInfo for a user
    @PostMapping
    public ResponseEntity<KidsInfo> addKidsInfo(@RequestBody KidsInfo kidsInfo, HttpServletRequest request) {
        String email = extractEmail(request);
        String token = request.getHeader("Authorization");

        getUserByEmail(email, token);

        kidsInfo.setUserEmail(email);
        return ResponseEntity.ok(kidsInfoService.createKidsInfo(kidsInfo));
    }

    // Get KidsInfo for a user based on email
    @GetMapping("/user")
    public ResponseEntity<List<KidsInfo>> getAllByUser(HttpServletRequest request) {
        String email = extractEmail(request);
        List<KidsInfo> list = kidsInfoService.getByUserEmail(email);
        return ResponseEntity.ok(list);
    }

    // Get all KidsInfo
    @GetMapping
    public ResponseEntity<List<KidsInfo>> getAll() {
        return ResponseEntity.ok(kidsInfoService.getAllKidsInfo());
    }

    // Get KidsInfo by ID
    @GetMapping("/{id}")
    public ResponseEntity<KidsInfo> getById(@PathVariable Long id) {
        Optional<KidsInfo> kid = kidsInfoService.getById(id);
        return kid.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Update KidsInfo by ID
    @PutMapping("/{id}")
    public ResponseEntity<KidsInfo> update(@PathVariable Long id, @RequestBody KidsInfo updated, HttpServletRequest request) {
        String email = extractEmail(request);

        if (!isUserAuthorized(id, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        updated.setUserEmail(email);
        KidsInfo result = kidsInfoService.updateKidsInfo(id, updated);
        return ResponseEntity.ok(result);
    }

    // Delete KidsInfo by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String email = extractEmail(request);

        if (!isUserAuthorized(id, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        kidsInfoService.deleteKidsInfo(id);
        return ResponseEntity.noContent().build();
    }
}