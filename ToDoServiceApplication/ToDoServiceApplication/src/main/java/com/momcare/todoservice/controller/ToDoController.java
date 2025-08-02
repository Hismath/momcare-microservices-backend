package com.momcare.todoservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.momcare.todoservice.model.ToDo;
import com.momcare.todoservice.model.UserDto;
import com.momcare.todoservice.security.JwtUtil;
import com.momcare.todoservice.service.ToDoServiceImpl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;


import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/todo")
public class ToDoController {

    @Autowired
    private ToDoServiceImpl tService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    // ✅ Extract email from token
    private String extractEmail(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractEmail(token);
        }
        throw new RuntimeException("Missing or invalid Authorization header");
    }

    // ✅ Create ToDo (email from token)
    @PostMapping
    public ResponseEntity<?> createToDo(@RequestBody ToDo toDo, HttpServletRequest request) {
        try {
            String email = extractEmail(request);

            // 🔁 Call user-service to get user ID by email
            String url = "http://user-service/api/users/email/" + email;

            // ✅ Forward Authorization header (to avoid 403)
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", request.getHeader("Authorization")); // forward JWT token

            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

            ResponseEntity<UserDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                UserDto.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Long userId = response.getBody().getId();
                toDo.setUserId(userId);
                return ResponseEntity.ok(tService.createToDo(toDo));
            } else {
                return ResponseEntity.status(404).body("User not found");
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error while creating ToDo: " + e.getMessage());
        }
    }


    // ✅ Get all ToDos for the logged-in user
    @GetMapping
    public ResponseEntity<List<ToDo>> getUserToDos(HttpServletRequest request) {
        try {
            String email = extractEmail(request);
            String url = "http://user-service/api/users/email/" + email;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", request.getHeader("Authorization"));
            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
            
            ResponseEntity<UserDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                UserDto.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Long userId = response.getBody().getId();
                List<ToDo> todos = tService.getUserToDos(userId);
                return todos.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(todos);
            } else {
                return ResponseEntity.status(404).build();
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log stack trace to console
            return ResponseEntity.status(500).body(null);
        }
    }


    // ✅ Update ToDo
    @PutMapping("/{id}")
    public ResponseEntity<ToDo> updateToDo(@PathVariable Long id, @RequestBody ToDo updatedToDo) {
        try {
            ToDo updated = tService.updateToDo(id, updatedToDo);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ Delete ToDo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteToDo(@PathVariable Long id) {
        tService.deleteToDo(id);
        return ResponseEntity.noContent().build();
    }
}
