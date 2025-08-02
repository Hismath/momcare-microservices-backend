package com.momcare.expense_service.service;


import com.momcare.expense_service.model.MonthlyExpense;
import com.momcare.expense_service.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository repository;

    // Save or update expense (auto-sums total)
    public MonthlyExpense saveOrUpdate(MonthlyExpense expense) {
        double total = expense.getCategoryAmounts().values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        expense.setTotal(total);

        // Check if entry exists for this user-month, update if present
        Optional<MonthlyExpense> existing = repository.findByUserEmailAndYearAndMonth(
                expense.getUserEmail(), expense.getYear(), expense.getMonth());
        existing.ifPresent(e -> expense.setId(e.getId()));

        return repository.save(expense);
    }

    // Fetch all expenses for a user
    public List<MonthlyExpense> getByUser(String email) {
        return repository.findByUserEmail(email);
    }

    // Fetch expense for a specific month (single entry if exists)
    public Optional<MonthlyExpense> getByUserAndMonth(String email, int year, int month) {
        return repository.findByUserEmailAndYearAndMonth(email, year, month);
    }

    // ✅ New: Monthly Summary for Report Service
    public Optional<MonthlyExpense> getMonthlySummary(String email, int year, int month) {
        List<MonthlyExpense> expenses = repository.findByUserEmail(email);

        // Filter by year & month (if multiple records exist)
        List<MonthlyExpense> monthlyExpenses = expenses.stream()
                .filter(e -> e.getYear() == year && e.getMonth() == month)
                .toList();

        if (monthlyExpenses.isEmpty()) {
            return Optional.empty();
        }

        // Aggregate category amounts
        Map<String, Double> categoryAmounts = monthlyExpenses.stream()
                .flatMap(e -> e.getCategoryAmounts().entrySet().stream())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingDouble(Map.Entry::getValue)
                ));

        // Compute total
        double total = categoryAmounts.values().stream().mapToDouble(Double::doubleValue).sum();

        MonthlyExpense dto = new MonthlyExpense();
        dto.setUserEmail(email);
        dto.setYear(year);
        dto.setMonth(month);
        dto.setCategoryAmounts(categoryAmounts);
        dto.setTotal(total);

        return Optional.of(dto);
    }
}
