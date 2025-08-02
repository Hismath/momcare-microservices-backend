package com.momcare.expense_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.momcare.expense_service.model.MonthlyExpense;
import com.momcare.expense_service.security.JwtUtil;
import com.momcare.expense_service.service.ExpenseService;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin("*")
public class ExpenseController {

    @Autowired
    private ExpenseService service;

    @Autowired
    private JwtUtil jwtUtil;

    private String extractUserEmail(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractEmail(token);
    }

    @PostMapping("/add")
    public ResponseEntity<MonthlyExpense> addExpense(@RequestBody MonthlyExpense expense, HttpServletRequest request) {
        String email = extractUserEmail(request);
        expense.setUserEmail(email);
        return ResponseEntity.ok(service.saveOrUpdate(expense));
    }

    @GetMapping("/user")
    public ResponseEntity<List<MonthlyExpense>> getExpensesByUser(HttpServletRequest request) {
        String email = extractUserEmail(request);
        return ResponseEntity.ok(service.getByUser(email));
    }

    // ✅ Existing path-based endpoint
    @GetMapping("/{year}/{month}")
    public ResponseEntity<?> getMonthlyExpense(HttpServletRequest request,
                                               @PathVariable int year,
                                               @PathVariable int month) {
        String email = extractUserEmail(request);
        return service.getByUserAndMonth(email, year, month)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyExpense> getMonthlyExpenseSummary(
            HttpServletRequest request,
            @RequestParam String email,
            @RequestParam int year,
            @RequestParam int month) {

        // Extract email from JWT to prevent spoofing
        String tokenEmail = extractUserEmail(request);
        if (!tokenEmail.equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return service.getMonthlySummary(email, year, month)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
