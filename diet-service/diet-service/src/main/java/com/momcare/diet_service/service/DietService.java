package com.momcare.diet_service.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.momcare.diet_service.model.DietEntry;
import com.momcare.diet_service.repository.DietRepository;

@Service
public class DietService {
	
	@Autowired
	private DietRepository dRepo;
	
	public List<DietEntry> findUserByEmail(String email)
	{
		
		return dRepo.findByUserEmail(email);
	}
	
	public Optional<DietEntry> findUserByEmailandDate(String email,LocalDate date)
	{
		return dRepo.findByUserEmailAndDate(email,date);
	}
	
	public List<DietEntry> getByMonth(String email, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return dRepo.findByUserEmailAndDateBetween(email, ym.atDay(1), ym.atEndOfMonth());
    }
	
	 public DietEntry saveOrUpdate(DietEntry entry) {
	        Optional<DietEntry> existing = dRepo.findByUserEmailAndDate(entry.getUserEmail(), entry.getDate());
	        existing.ifPresent(e -> entry.setId(e.getId()));
	        return dRepo.save(entry);
	    }


}
