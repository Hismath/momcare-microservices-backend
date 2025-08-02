package com.momcare.diet_service.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.momcare.diet_service.model.DietEntry;
import com.momcare.diet_service.security.JwtUtil;
import com.momcare.diet_service.service.DietService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/diet")
@CrossOrigin("*")
public class DietController {

    @Autowired
    private DietService service;

    @Autowired
    private JwtUtil jwtUtil;

    private String extractEmail(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractEmail(token);
    }

    @PostMapping("/log")
    public ResponseEntity<DietEntry> logDiet(@RequestBody DietEntry entry, HttpServletRequest request) {
        entry.setUserEmail(extractEmail(request));
        return ResponseEntity.ok(service.saveOrUpdate(entry));
    }

    @GetMapping("/all")
    public ResponseEntity<List<DietEntry>> getAll(HttpServletRequest request) {
        return ResponseEntity.ok(service.findUserByEmail(extractEmail(request)));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<?> getByDate(@PathVariable String date, HttpServletRequest request) {
        return service.findUserByEmailandDate(extractEmail(request), LocalDate.parse(date))
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body("No entry found"));
    }

    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<List<DietEntry>> getByMonth(@PathVariable int year, @PathVariable int month, HttpServletRequest request) {
        return ResponseEntity.ok(service.getByMonth(extractEmail(request), year, month));
        
        
    }
    
    @GetMapping("/monthly")
    public ResponseEntity<List<DietEntry>> getMonthlyDietEntries(
            HttpServletRequest request,
            @RequestParam String email,
            @RequestParam int year,
            @RequestParam int month) {

        // Verify email from JWT matches request param (prevent spoofing)
        String tokenEmail = extractEmail(request);
        if (!tokenEmail.equals(email)) {
            return ResponseEntity.status(403).build();
        }

        List<DietEntry> entries = service.getByMonth(email, year, month);
        return ResponseEntity.ok(entries);
    }

}
