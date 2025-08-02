package com.momcare.diet_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DietEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userEmail; 

    private LocalDate date;

    @ElementCollection
    @CollectionTable(name = "meal_entries", joinColumns = @JoinColumn(name = "diet_id"))
    @MapKeyColumn(name = "meal_type")
    @Column(name = "meal_details")
    private Map<String, String> meals = new HashMap<>();

    @Column(name = "water_intake_in_litres", nullable = true)
    private Double waterIntakeInLitres;

    
}
