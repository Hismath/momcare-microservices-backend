package com.momcare.todoservice.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.momcare.todoservice.model.ToDo;
import com.momcare.todoservice.repository.ToDoRepository;

import java.util.List;

@Service
public class ToDoServiceImpl implements ToDoService {
	
	@Autowired
	private ToDoRepository tRepo;

	@Override
	public ToDo createToDo(ToDo toDo) {
	
		return tRepo.save(toDo);
	}

	@Override
	public List<ToDo> getUserToDos(Long userId) {
		
		return tRepo.findByUserId(userId);
	}

	@Override
	public ToDo updateToDo(Long id, ToDo toDo) {
		
		ToDo existing=tRepo.findById(id).orElseThrow();
		existing.setTitle(toDo.getTitle());
		existing.setDescription(toDo.getDescription());
		existing.setCompleted(toDo.isCompleted());
			
		return tRepo.save(existing);
	}

	@Override
	public void deleteToDo(Long id) {
		
		 tRepo.deleteById(id);
		
	}

   
}
