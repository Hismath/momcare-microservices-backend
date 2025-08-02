package com.momcare.diet_service.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.momcare.diet_service.model.DietEntry;

public interface DietRepository extends JpaRepository<DietEntry, Long> {

    List<DietEntry> findByUserEmail(String userEmail);

    Optional<DietEntry> findByUserEmailAndDate(String userEmail, LocalDate date);

    List<DietEntry> findByUserEmailAndDateBetween(String userEmail, LocalDate start, LocalDate end);
}

