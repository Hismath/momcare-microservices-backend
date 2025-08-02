package com.momcare.expense_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;
    private int year;
    private int month;

    @ElementCollection
    @CollectionTable(name = "expense_categories", joinColumns = @JoinColumn(name = "expense_id"))
    @MapKeyColumn(name = "category")
    @Column(name = "amount")
    private Map<String, Double> categoryAmounts;
    private double total;
}
