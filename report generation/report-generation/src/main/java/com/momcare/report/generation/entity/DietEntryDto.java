package com.momcare.report.generation.entity;

import lombok.Data;
import java.time.LocalDate;
import java.util.Map;

@Data
public class DietEntryDto {
    private String userEmail;
    private LocalDate date;
    private Map<String, String> meals;
    private Double waterIntakeInLitres;
}