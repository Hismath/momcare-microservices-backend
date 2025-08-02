package com.momcare.report.generation.entity;

import lombok.Data;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MonthlyExpenseDto {
    private String userEmail;
    private int year;
    private int month;
    private Map<String, Double> categoryAmounts;
    private double total;
    
    public MonthlyExpenseDto(String userEmail, int year, int month, Map<String, Double> categoryAmounts, double total) {
        this.userEmail = userEmail;
        this.year = year;
        this.month = month;
        this.categoryAmounts = categoryAmounts;
        this.total = total;
    }
}