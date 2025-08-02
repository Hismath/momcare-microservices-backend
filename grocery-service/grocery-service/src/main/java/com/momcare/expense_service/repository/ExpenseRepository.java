package com.momcare.expense_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.momcare.expense_service.model.MonthlyExpense;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<MonthlyExpense, Long> {
    List<MonthlyExpense> findByUserEmail(String email);
    Optional<MonthlyExpense> findByUserEmailAndYearAndMonth(String email, int year, int month);
}
